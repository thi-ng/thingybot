# thingybot

A modular, command driven Twitter bot, currently only generating
visualizations of L-Systems, though easily adaptable for more
interesting purposes.

- Configurable poll time (not using streaming api (yet))
- Can send media tweets (w/ image attachments)
- Keeps track of last processed tweet ID (to avoid duplicates)
- Adds proper source tweet ID for replies

[@thingybot](https://twitter.com/thingybot)

## Usage

Overall tweet syntax is:

```
@botname command,command-specific-arguments
```

### L-System generation

Example tweet to generate an L-System visualization **(note: the bot is
not running 24/7 at the moment)**:

```
@thingybot lsys,#fff,16,60,90:s=a,a=a+b+,b=-a-b
```

The overall format for the `lsys` command is: `header:rules`, where
`header` is a comma separated list of:

- CSS hex color string
- Number of iterations
- Start angle
- Rotation angle

...and `rules` a comma separated list of single letter rule IDs an
their replacements. The ruleset **must** contain a rule named `s` -
the start rule (axiom).

Other rule symbols are:

- `f` - forward (draw line)
- `-` - rotate left
- `+` - rotate right
- `[` - store state (e.g. for branching)
- `]` - pop state (end branch)

Furthermore the symbols `a` - `e` can be used as markers or for more
complex, mutually recursive replacements and each also execute an
implicit forward motion.

### Error handling

For security reasons the bot will refuse to execute systems expanding
to more than 1 million symbols.

If there're any syntax errors, you'll most likely get a text-only reply telling you so, or if the error is more subtle, receive an image with default values for the ones which contained errors (e.g. header settings).

## Running

First edit `launch-sample.sh` and add your Twitter API details (see
below). Save file as `launch.sh`, then launch via:

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
