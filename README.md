# Skyblock-Bazaar-Status
Android app for taking raw JSON data from the Hypixel Bazaar API and making it usable.

Just for fun, I decided to implement a decent number of additional features, including Google Play Achievements.

To read more detail about what I did, and how I solved problems I encountered, check out this document:

https://docs.google.com/document/d/1T0r9PfIG22_gqgKuD8D8gZbekgcGNyFVupz5ELAFWSs/

If you don't know what Hypixel Skyblock is, it is a Minecraft server, and a major point of the game is

resource collection, and trading with other players.

There are "minions" that all produce materials at different speeds, and not only that, the material cost constantly

fluctuates with player demand.

This app takes that data, and can help calculate for you the minion that will give you the most money if you were to

sell it on the in game commodity market (the bazaar), which this app retrieves live data from.

Furthermore, you can just straight up view whatever materials that are currently being sold, and I have provided what

I believe to be an easier way to view it all.

Another feature I recently added was the ability to build up a history of the prices of all the in game items.
I created a database so that price information can be efficiently stored, and I wrote the code so that when new items
are added (they often are) it integrates flawlessly into the existing database.
This data is used to give the user insights into price changes, as I set it up so that you can have your phone automatically
retrieve price data from the API at your chosen interval. When it does so, I have it set up so that you can also receive 
notifications on your phone when prices change enough for you to care.
Considering there are hundreds of products, this can provide information at a glance that will allow users to
maximize the amount of money players can make while playing the game.

If I have applied to your job and I linked this as an accomplishment, I hope you find it impressive.
