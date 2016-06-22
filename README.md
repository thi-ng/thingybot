# thingybot

A little Twitter bot, currently purely translating messages into
pseudo Early Modern English, though easily adaptable for more
interesting purposes.

- Configurable poll time (not using streaming api (yet))
- Keeps track of last processed reply (to avoid duplicates)
- Adds proper source tweet ID for replies

[@thingybot](https://twitter.com/thingybot)

## Usage

First edit `launch-sample.sh` and add your Twitter API details (see below). Then launch via:

```
./launch.sh repl
```

In the REPL kick off with:

```clj
;; start bot w/ 60s poll interval
;; (shorter periods will trigger Twitter's API rate limit)
(def bot (run state 60000))

;; `bot` is a control channel, which should be closed to stop the bot
(close! bot)
```

## Create Twitter app credentials

1. Go to: https://apps.twitter.com/
2. Create new app (don't need callback URL)
3. Copy Consumer key/secret & Access token/secret into `launch-sample.sh`
4. Change the bot user name

## License

Copyright © 2015 Karsten Schmidt

Distributed under the
[Apache Software License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
