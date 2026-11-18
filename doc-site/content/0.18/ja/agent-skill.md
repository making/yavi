# エージェントスキル

Bean Validation を知っている AI コーディングエージェントでも、YAVI のコードは書き間違えます。オブジェクトを作ってから検証し、その型にない制約に手を伸ばし、もう存在しない `getErrors()` で結果を処理しようとします。**エージェントスキル**はその差を埋めます。中身はこのドキュメントをそのままパッケージした Claude スキルで、作業上のルールを記した `SKILL.md` と、ここにある全ページを参照用に同梱したものです。デプロイのたびに同じ Markdown から生成しているので、いま読んでいるドキュメントから内容がずれることはありません。

## Claude Code へのインストール

スキルはプラグインとして公開しています。マーケットプレイスを追加してインストールします:

```bash
claude plugin marketplace add https://yavi.ik.am/skill/marketplace.json
claude plugin install yavi@yavi
```

同じ 2 手順はセッション内でも `/plugin marketplace add ...` と `/plugin install yavi@yavi` として実行できます。最初のコマンドに `--scope project` を付けると、ユーザー設定ではなくリポジトリ側にマーケットプレイスを宣言できるので、そのチェックアウトで作業する全員に同じものが提示されます。

他に設定は要りません。`Yavi.schema()` のチェーン、`ValidatorBuilder`、あるいはライブラリ名を含む依頼など、YAVI が関わる作業ではエージェントが自分でスキルを参照します。

```bash
claude plugin update yavi@yavi     # 新しいバージョンを取得
claude plugin uninstall yavi@yavi  # 削除
claude plugin list                 # インストール済みの一覧
```

Claude Code はその URL からマーケットプレイスのファイルを読み直すため、こちらで何もしなくても新しいバージョンが利用可能になります。バージョンの上がり方は[最新に保つ](#staying-current)を参照してください。

## プラグインを使わないインストール

スキルはディレクトリであり、Claude Code は `~/.claude/skills`（ユーザー）と `.claude/skills`（リポジトリ）配下のスキルをすべて読み込みます。マーケットプレイスを登録せず、置くだけで済ませたい場合は:

```bash
mkdir -p ~/.claude/skills && \
  curl -sSL https://yavi.ik.am/skill/yavi-skill.tar.gz | tar xz -C ~/.claude/skills
```

展開先を `.claude/skills` に変えれば、プロジェクト側へのインストールになります。どちらの場合も `skills/yavi/SKILL.md` と `skills/yavi/references/` ができます。読み込まれているものは `/skills` で一覧でき、`yavi` ディレクトリを削除すればアンインストールです。自動では更新されないので、この方法のときは自分でバージョンを確認してください。

## 他のエージェントとホスト

| ファイル | 用途 |
| --- | --- |
| [marketplace.json](https://yavi.ik.am/skill/marketplace.json) | 上記のように URL で追加するプラグインのマーケットプレイス |
| [yavi-plugin.zip](https://yavi.ik.am/skill/yavi-plugin.zip) | プラグイン本体。別の方法でプラグインを入れる場合に |
| [yavi-skill.tar.gz](https://yavi.ik.am/skill/yavi-skill.tar.gz) | スキルのディレクトリそのもの。`skills` フォルダに入れる場合に |
| [yavi.skill](https://yavi.ik.am/skill/yavi.skill) | 同じツリーを zip にしたもの。展開ではなくアップロードする先に |
| [SKILL.md](https://yavi.ik.am/skill/yavi/SKILL.md) | スキル本文のみ。その場で読む場合に |
| [yavi-full.md](https://yavi.ik.am/skill/yavi-full.md) | ドキュメントとスキルを 1 つの Markdown にまとめたもの。スキルローダーのないツールに |

## 最新に保つ

スキルのバージョンは `<ドキュメントのバージョン>.<内容を変えうるコミット数>` です。ページか生成器が動いたときにだけ上がります。再ビルドしてもバイト単位で同じものができる場合は公開済みのバージョンをそのまま残すため、内容の変わっていないドキュメントが全インストールに再ダウンロードを求めることはありません。プラグインなら `claude plugin update yavi@yavi` で新しいものを取得できます。手で置いた場合は、自分のコピーと公開されているバージョンを比べ、違っていれば入れ直してください:

```bash
head -3 ~/.claude/skills/yavi/SKILL.md            # version: 0.18.42
curl -sSL https://yavi.ik.am/skill/VERSION
```

入れ直すコマンドはインストールと同じで、その場で上書きされます。スクリプトから確認したい場合は、同じバージョンにビルド元のコミットと内容のハッシュを加えた [version.json](https://yavi.ik.am/skill/version.json) があります。

## 中身

`SKILL.md` には、Bean Validation や Zod の癖を持つエージェントが間違えやすいルールを書いています。どちらのスタイルを選ぶか、スキーマは `fromMap` か `apply` で終えるまでバリデータではないこと、必須プロパティを `notNull()` では書かないこと、`Validated` は網羅的な `switch` で処理すること、Kotlin のヘルパーがどこで違うか、などです。クイックスタートとして[はじめに](getting-started.md)をそのまま取り込んでいます。また、リクエスト境界の形として、スキーマ、`@ModelAttribute`、`BindingResult`、Thymeleaf のフォームまでを含む Spring MVC のコントローラの完全な例も載せています。`references/` には次を置いています:

- `contents.md`、このドキュメントの全ページのタイトル一覧。
- このドキュメントの全ページを、同じ相対パスでそのまま。スキルが必要とする詳細はネットワークなしのファイル読み取りで届きます。
- サイトの実行可能なサンプルのページを、そのまま。期待する出力は非表示の `run:` コメントに残り、`Run` ボタンだけがありません。

スキルが同梱するのは最新のドキュメント 1 バージョンの英語版です。
