日本語 | [English](docs/README_EN.md)

# CommandUtility

CommandUtilityは、Bukkitでのコマンド処理を改善するために開発されたライブラリです。  
独自のコマンドハンドラを使用することで、コマンドの実行や、Tab補完機能が簡単に実装できるようになります。

## 導入方法

~~[ここ](https://jitpack.io/#kuro46/CommandUtility)に従って依存関係を追加してください。  
注意: このライブラリはプラグインではないため、必ずshadeプラグインなどでjarファイル内に格納してください。~~

**開発中のため非推奨**

## 例

Minecraft標準の`/gamemode`コマンドをこのライブラリを使って再実装する例です。  
ただし、以下のように変更します。

- `/gamemode`のかわりに、`/mycmd gamemode`でゲームモード変更、`/mycmd help`で`/mycmd`のサブコマンドリストとその説明を表示、それ以外の不明なコマンドであれば、`不明なコマンドです`と表示する
- `/mycmd gamemode`の実行者はプレイヤーのみ、それ以外の場合は`ゲームから実行してください`と表示する
- 引数が足りない場合、`引数が足りません`と表示する
- 引数が多すぎる場合、`引数が多すぎます`と表示する

まず、`/mycmd gamemode`コマンドを処理するハンドラと、`/mycmd help`コマンドを処理するハンドラを作成します。

```kotlin
// /mycmd gamemodeコマンドを処理するハンドラ
val gameModeHandler = object : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().apply {
        addArgument(RequiredArgument("モード") /* 必須の引数。無ければCommandHandlerManager#handleParseErrorが実行される */)
        addArgument(OptionalArgument("プレイヤー") /* 任意の引数。無ければargsがnullになる */)
        // RequiredArgumentクラスやOptionalArgumentクラスはスペースを許容しないが、LongArgumentを追加することでスペース入りの引数を取得することができる
    }.build()

    override val senderType = CommandSenderType.PLAYER

    override fun handleCommand(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: Map<String, String>
    )
        val executor: Player = sender as Player // 事前にキャスト可能かどうかチェックされるためキャスト可能 (キャスト不可であればこの関数は呼び出されず、かわりにCommandHandlerManager#handleCastErrorが呼び出される)
        val gameMode: GameMode = args.getValue("モード").toUpperCase() // non-null (指定が無ければこの関数は呼び出されず、かわりにCommandHandlerManager#handleParseErrorが呼び出される)
            .let {
                try {
                    GameMode.valueOf(it)
                } catch (ignored: IllegalArgumentException) {
                    executor.sendMessage("「${it}」は有効な数値ではありません")
                    return
                }
            }
        val target: Player = args["プレイヤー"] // nullable (指定がなかった場合にnull)
            ?.let {
                Bukkit.getPlayer(it) ?: run {
                    executor.sendMessage("プレイヤー「${it}」は見つかりませんでした")
                    return
                }
            }
            ?: executor

        target.setGameMode(gameMode)
        target.sendMessage("ゲームモードが${gameMode}モードに変更されました")
    }

    // abstractでないため未実装も可。デフォルトではサブコマンドリストが送信される
    override fun handleTabComplete(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        completionData: CompletionData
    ): List<String> {
        val (name: String /* 引数名 */, currentValue: String /* 現在の値 */) = completionData.notCompletedArg // 補完中の引数の名前と現在の値

        return when (name) {
            "モード" -> GameMode.values().map { it.name }
            "プレイヤー" -> Bukkit.getOnlinePlayers().map { it.name }
            else throw IllegalArgumentException()
        }
    }
}

// /mycmd helpを処理するハンドラ
val helpHandler = object : CommandHandler() {

    override val commandSyntax = CommandSyntaxBuilder().build() // 引数は取らないためそのままbuild()する

    override val senderType = CommandSenderType.ANY // コンソールからでもゲームからでも実行可能

    override fun handleCommand(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: Map<String, String>
    ) {
        sender.sendMessage("/mycmd gamemode ${gameModeHandler.commandSyntax} - ゲームモードを変更します。")
    }
}
```

次に、不明なコマンドを処理する、FallbackCommandHandlerを初期化します

```kotlin
val fallbackHandlerImpl = object : FallbackCommandHandler(){

    override val senderType = CommandSenderType.ANY

    override fun handleFallback(
        caller: CommandHandlerManager,
        sender: CommandSender,
        command: Command,
        args: List<String>
    ) {
        sender.sendMessage("不明なコマンドです")
    }
}
```

最後に、キャスト失敗時や、不正な引数が渡された時の処理を行ったり、CommandHandlerを登録したりするためのCommandHandlerManagerを初期化(実装)し、CommandHandlerを登録していきます。

初期化
```kotlin
val manager = object : CommandHandlerManager(plugin /* org.bukkit.plugin.Plugin */) {

    override val fallbackHandler = fallbackHandlerImpl // FallbackCommandHandlerの登録

    // キャスト失敗時に呼び出される
    override fun handleCastError(sender: CommandSender, castError: CastError) {
        when (castError) {
            CastError.CANNOT_CAST_TO_PLAYER -> { // プレイヤーへのキャスト失敗
                sender.sendMessage("ゲームから実行してください")
            }
            CastError.CANNOT_CAST_TO_CONSOLE -> { // ConsoleCommandSenderへのキャスト失敗
                sender.sendMessage("コンソールから実行してください")
            }
        }
    }

    // 引数のパース失敗時(引数が足りない、または多すぎる場合)に呼び出される
    override fun handleParseError(sender: CommandSender, parseError: ParseErrorReason) {
        when (parseError) {
            ParseErrorReason.ARGUMENTS_NOT_ENOUGH -> { // 引数が足りない
                sender.sendMessage("引数が足りません")
            }
            ParseErrorReason.TOO_MANY_ARGUMENTS -> { // 引数が多すぎる
                sender.sendMessage("引数が多すぎます")
            }
        }
    }
}

// BukkitへのCommandExecutor/TabCompleterの登録はCommandHandlerManger#registerHandlerの呼び出し時に自動で行われるため必要ない
```

ハンドラの登録
```kotlin
manager.registerHandler("mycmd gamemode", gameModeHandler)
manager.registerHandler("mycmd help", helpHandler)
```
