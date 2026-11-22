# リクエストパラメータとフォーム

`readingParameters()` は、サーブレットリクエストや送信されたフォームが提供する文字列のフラットな Map を、Map や JSON 文書を読むのと同じスキーマで読み取ります。文字列はプロパティの型の値を表すテキストであり、キーはネストした値のパスです。

```java
record SearchForm(String q, Integer page, Boolean includeArchived, @Nullable LocalDate since, List<String> tags,
        @Nullable Address address) {}

ValueValidator<Map<String, ?>, SearchForm> searchFormValidator = Yavi.schema()
        ._string("q", c -> c.notBlank())
        ._integer("page", c -> c.greaterThanOrEqual(1)).defaultValue(1)
        ._boolean("includeArchived").defaultValue(false)
        ._localDate("since").nullable()
        ._list("tags", Yavi._string(c -> c.notBlank())).defaultValue(List.of())
        ._object("address", addressValidator).nullable()
        .fromMap(SearchForm::new)
        .readingParameters();

// ?q=yavi&page=2&since=&tags=a&tags=b&address.city=Tokyo
Validated<SearchForm> validated = searchFormValidator.validate(params);
```

このような Map のすべての形式を受け付けます。`Map<String, String>`、Spring の `MultiValueMap` のような `Map<String, List<String>>`、Servlet API の `Map<String, String[]>` です。代入先に関係なく型は `ValueValidator<Map<String, ?>, X>` です。

## キーとパス

* `address.city` と `address[city]` はオブジェクト `address` のプロパティ `city` です。`tags[]` は `tags` です。
* 複数の値を持つキーはリストです（`tags=a&tags=b`）。スカラーのプロパティに複数の値があると `object.type` で報告され、どれも選ばれません。
* インデックス付きのキーはインデックスの順のリストです（`items[0].name=pen&items[1].name=ink`）。
* 飛ばされたインデックスは `null` の要素になるため、違反は送られたインデックスを保ちます（`items[2].name`）。
* ドットを含む名前はブラケットで書きます（`scores[a.b]`）。
* 値とネストしたキーの両方を持つキー（`a=1&a.b=2`）は `object.type` で報告され、パスとして書かれていないキー（`a..b`、`a[0`）はどのプロパティにも一致しません。
* `rejectingUnknownProperties()` を指定すると、未知のキーはそれを持つオブジェクトのバリデータが報告します（`address.zip`）。

## 変換と StringCoercion

デフォルトのルールは厳格です。

* 数値は `new BigDecimal(String)` の文法の十進数です。任意の符号、数字、任意の小数部、任意の指数を含み、前後の空白や桁区切りはありません。`"30"` と `"30.0"` は `Integer` で、`"30.5"`、`" 30"`、`"1,000"` はそうではありません。
* 真偽値は `"true"` または `"false"` です。
* enum 定数・時刻値・`UUID`・`URI` は [Map](input-maps.md) と同じく文字列から読み取られます。変換が加えるのは、Map では固有の書き方を持つ型だけです。
* 型を表さない文字列は例外ではなく `object.type`（`"page" must be of type integer`）で報告されます。文字列への変換は行われません。

`StringCoercion` は、フォームのフィールドなど人が書いたテキストのためにルールを緩めます。

```java
StringCoercion form = StringCoercion.strict().trimming().booleanWords();

ValueValidator<Map<String, ?>, Signup> signupValidator = Yavi.schema()
        ._integer("age")
        ._boolean("subscribed").defaultValue(false) // an unchecked checkbox sends nothing
        .fromMap(Signup::new)
        .readingParameters(form);

signupValidator.validate(Map.of("age", " 30 ", "subscribed", "on")); // Signup[age=30, subscribed=true]
```

* `trimming()` はプロパティの型に関係なくすべてのテキストを前後の空白なしで読み取り、空白だけの文字列を `null` として検証します。制約はトリムされたテキストを見ます。
* `booleanWords()` は `true`、`1`、`yes`、`on` と `false`、`0`、`no`、`off` を大文字小文字を問わず受け付けます。`booleanWords(trueWords, falseWords)` はアプリケーションの語を取ります。
* パスワードのように空白が値の一部である `String` は、そのバリデータの `untrimmed()` でそう示します（`._string(Yavi._string("password", c -> c.greaterThanOrEqual(8)).untrimmed())`）。
* 各ルールは一度だけ与えます。`reader.coercingStrings(rules).coercingStrings()` と `booleanWords(...).booleanWords()` はコンパイルできません。
* バリデータは最後に与えられたリーダーで読み取ります。`readingParameters()` の後の `readingWith(reader)` は文字列を変換しません。

## 空の値と既定値

* 空文字列はプロパティの型に関係なく `null` として検証されます。`optional()`、`nullable()`、`defaultValue(...)` が適用されます。`""` を受け取る必要がある文字列プロパティには `defaultValue("")` を与えます。
* テキストに `null` はないため、空文字列を `object.notNull` で拒否するプロパティは、存在しないキーと同様に `object.required`（`"page" is required`）を報告します。`notBlank()` のように `null` を拒否する制約の違反はそのまま報告されます。
* 空のリストやオブジェクトは送信できません。キーが存在しない状態になり、`null` として検証されます。チェックボックスのよう空でありうるリストには `defaultValue(List.of())` を与えます。
* `recover` はリクエストを失敗させてはならないパラメータに適しています（`._integer("size", c -> c.greaterThanOrEqual(1).lessThanOrEqual(100)).recover(20)`）。

## ラベルとメッセージ

フォームはフィールドをラベルで呼びます。`label` はプロパティとその要素のすべてのメッセージの `{0}` を変更します。違反の名前、パス、ポインタは変わりません。`typeMessage` は `object.type` のメッセージを、`required` は `object.required` のメッセージを置き換えます。

```java
record Token(String name, Integer ttl, List<String> roles) {}

ValueValidator<Map<String, ?>, Token> tokenValidator = Yavi.schema()
        ._string("name", c -> c.notBlank()).label("Token name")
        ._integer("ttl", c -> c.positive()).typeMessage("\"{0}\" must be a number of seconds")
        ._list("roles", Yavi._string(c -> c.notBlank())).label("Roles").required("Select at least one role")
        .fromMap(Token::new)
        .readingParameters()
        .formattingWith(new MessageSourceMessageFormatter(messageSource::getMessage));
```

フォーマッターと、コードに書いたメッセージに関するルールは[メッセージとローカライズ](messages.md)を参照してください。

## 1 つのパラメータのリスト

`_list` の後の `splitBy` は 1 つのパラメータをリストとして読み取ります。`?tags=java,yavi`、`?ids=1,2,3`、または `textarea` の行ごとの URI です。

```java
ValueValidator<Map<String, ?>, Article> articleValidator = Yavi.schema()
        ._string("title", c -> c.notBlank())
        ._list("tags", Yavi._string(c -> c.notBlank().lessThanOrEqual(10)), c -> c.unique()).splitBy(",")
        ._list("related", Yavi._integer(c -> c.positive())).splitBy(",").defaultValue(List.of())
        ._list("links", Yavi._object(URI.class)).splitBy(Pattern.compile("\\R")).defaultValue(List.of())
        .fromMap(Article::new)
        .readingParameters();
```

* 区切りはリテラルのテキストか `Pattern` です。要素は値を表すテキストであり、違反はそのインデックスの下（`tags[1]`）に置かれます。
* すべてのテキストが分割されます。繰り返しキーの値も同様で、`tags=a,b&tags=c` は `[a, b, c]` です。
* 要素は削除もトリムもされません。`a,,b` は 3 つの要素を持ち、2 番目は値を持ちません。末尾の区切りは要素を開始しません。`a,b,` は `[a, b]` です。
* 空のパラメータ（`tags=`）は値を持ちません。
* `splitBy` は `_list` の後、他の修飾子の前に書きます。その前に書けるのは `label` と `typeMessage` だけです。
* テキストでもリストでもない値は `object.type`（`"tags" must be of type array or string`）で報告され、1 要素のリストとはみなされません。

## 制限と安全性

* インデックスは最大 255（`ParameterTree.DEFAULT_MAX_INDEX`）です。それより大きい数値は名前になるため、`tags[999999999]` は `object.type` で報告され、何も確保しません。
* `ParameterTree.of(map, maxIndex)` は別の制限を与えます。バリデータが別の制限でツリーを読む場合は、リーダーにも同じ制限を与えます。
* 飛ばされたインデックスは `null` の要素です。リストは送信された最大インデックスの長さになるため、制限は信頼できないリクエストが確保できる量を制限します。
* パスでないキーはどのプロパティにも一致せず無視されるか、`rejectingUnknownProperties()` により報告されます。
* `readingParameters()` は Map の `ParameterTree` を検証します。任意のバリデータは `readingWith(ParameterTree.reader())` でそれを読み取れます。

## パラメータの書き戻し

有効でないフォームは、フィールドを手でコピーすることなく、送信されたテキストで再表示されます。`ParameterTree` はスキーマの形でパラメータを返し、パラメータを欠くリクエストにも同じ呼び出しで空のフォームを返します。[オブジェクトの書き戻し](writing-back.md#parameters-and-forms)を参照してください。
