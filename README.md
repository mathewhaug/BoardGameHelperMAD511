# Board Game Helper

To use the real BoardGameGeek API, add your own token to `local.properties` as `BGG_TOKEN=your_token` (the file is gitignored, so it never gets committed).

If `BGG_TOKEN` is blank or missing, the app does not call BoardGameGeek at all and loads the hand written sample responses in `app/src/main/assets` instead.
