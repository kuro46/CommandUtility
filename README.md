日本語 | [English](docs/README_EN.md)

# CommandUtility [![Latest release](https://img.shields.io/github/v/release/kuro46/CommandUtility)](https://github.com/kuro46/CommandUtility/releases)[![GitHub Workflows](https://github.com/kuro46/CommandUtility/workflows/Build/badge.svg)](https://github.com/kuro46/CommandUtility/actions)

CommandUtilityは、Bukkitでのコマンド処理を改善するために開発されたライブラリです。  
独自のコマンドハンドラを使用することで、コマンドの実行処理や、Tab補完機能が簡単に実装できるようになります。

## 導入方法

### Maven

Mavenの場合、まずリポジトリを以下のように追加し、
```xml
<repository>
    <id>shirokuro-repo</id>
    <url>https://maven.shirokuro.dev/repos/releases/</url>
</repository>
```
以下のように依存関係に追加してください。
```xml
<dependency>
    <groupId>dev.shirokuro</groupId>
    <artifactId>commandutility</artifactId>
    <version>最新のバージョン</version>
</dependency>
```
また、必要に応じてmaven-shade-pluginなどを使用してjar内に追加してください。

### Gradle

Gradle(Groovy DSL)の場合、まずリポジトリを以下のように追加し、
```groovy
maven { url 'https://maven.shirokuro.dev/repos/releases/' }
```
以下のように依存関係に追加してください。
```groovy
implementation 'dev.shirokuro:commandutility:最新のバージョン'
```
また、必要に応じてshadowプラグインなどを使用してjar内に追加してください。

## 使用方法

以下は`/foo info <player>`でそのプレイヤーの現在地を表示するコマンドと、`/foo help`でヘルプメッセージを表示するコマンドの例です。

```java
import dev.shirokuro.commandutility.CommandExecutionException;
import dev.shirokuro.commandutility.CommandGroup;
import dev.shirokuro.commandutility.CommandUtils;
import dev.shirokuro.commandutility.ExecutionData;
import dev.shirokuro.commandutility.HelpCommandHandler;
import dev.shirokuro.commandutility.annotation.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FooPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new CommandGroup("[foo] "/*エラー時(コマンドが見つからなかった場合、引数のパースに失敗した場合)のプレフィックス*/)
            // ヘルプメッセージを自動生成する
            .add("foo help", new HelpCommandHandler())
            // @Executorもしくは@Completerアノテーションが付いたメソッドをすべて追加する
            .addAll(this);
    }

    @Executor(command = "foo info <player:players>", description = "Displays location of specified player.")
    public void info(ExecutionData data) throws CommandExecutionException {
        CommandSender sender = data.getSender();
        Player target = CommandUtils.toPlayer(data.get("player"), name -> {
            return "Cannot find player named " + name;
        });
        sender.sendMessage(String.format("World: %s X: %s Y: %s Z: %s",
            target.getWorld().getName(),
            target.getLocation().getX(),
            target.getLocation().getY(),
            target.getLocation().getZ()));
    }
}
```

また、以下のようにアノテーションを使わずに実装することも可能です。

```java
import dev.shirokuro.commandutility.CommandExecutionException;
import dev.shirokuro.commandutility.CommandGroup;
import dev.shirokuro.commandutility.CommandHandler;
import dev.shirokuro.commandutility.CommandUtils;
import dev.shirokuro.commandutility.ExecutionData;
import dev.shirokuro.commandutility.HelpCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FooPlugin extends JavaPlugin implements CommandHandler {
    @Override
    public void onEnable() {
        new CommandGroup("[foo] "/*エラー時(コマンドが見つからなかった場合、引数のパースに失敗した場合)のプレフィックス*/)
            .add("foo help", new HelpCommandHandler())
            .add(this, "foo info <player:players>", "Displays location of specified player.");
    }

    @Override
    public void execute(ExecutionData data) throws CommandExecutionException {
        CommandSender sender = data.getSender();
        if (data.getCommand().sections().equals("foo info")) {
            // toPlayerは、渡されたプレイヤー名のプレイヤーが存在すればPlayerインスタンスを返し、
            // 見つからなかった場合はCommandExecutionExceptionを投げる。
            // 尚、投げられたCommandExecutionExceptionはCommandGroup.errorPrefixとともにプレイヤーに送信される
            Player target = CommandUtils.toPlayer(data.get("player"), name -> {
                return "Cannot find player named " + name;
            });
            sender.sendMessage(String.format("World: %s X: %s Y: %s Z: %s",
                target.getWorld().getName(),
                target.getLocation().getX(),
                target.getLocation().getY(),
                target.getLocation().getZ()));
        } else {
            throw new RuntimeException("unreachable");
        }
    }
}
```
