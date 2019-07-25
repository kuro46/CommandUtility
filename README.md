日本語 | [English](docs/README_EN.md)

# CommandUtility [![CircleCI](https://circleci.com/gh/kuro46/CommandUtility.svg?style=svg)](https://circleci.com/gh/kuro46/CommandUtility)

CommandUtilityは、Bukkitでのコマンド処理を改善するために開発されたライブラリです。  
独自のコマンドハンドラを使用することで、コマンドの実行処理や、Tab補完機能が簡単に実装できるようになります。

## 導入方法

[ここ](https://jitpack.io/#kuro46/CommandUtility)に従って依存関係を追加してください。

注意: このライブラリはプラグインではないため、必ずshadeプラグインなどでjarファイル内に格納してください。  

また、無ければKotlinを依存関係に追加してください。

```kotlin
// build.gradle.kts
implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.41")
```

```groovy
// build.gradle
implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.41'
```

```xml
// pom.xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib-jdk8</artifactId>
    <version>1.3.41</version>
</dependency>
```

## 使用例

[このリポジトリ](https://github.com/kuro46/CommandUtilityExample)を参照してください。
