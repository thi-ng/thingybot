# thingybot

A little Twitter bot translating messages into pseudo Early Modern English.

[@thingybot](https://twitter.com/thingybot)

## Usage

First edit `launch-sample.sh` and add your Twitter API details. Then launch via:

```
./launch.sh repl
```

In the REPL kick off with:

```clj
;; start bot w/ 60s poll interval (shorter periods will trigger Twitter's API rate limit)
(def bot (run state 60000))

;; `bot` is a control channel, which should be closed to stop the bot
(close! bot)
```

## License

Copyright © 2015 Karsten Schmidt

Distributed under the
[Apache Software License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
