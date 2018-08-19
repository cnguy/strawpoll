# strawpoll

This will be a small [strawpoll](https://strawpoll.me) clone, that will not have the authentication features since I suck at that. This is an educational project that will eventually be hosted.

## Reasoning for Language Choices

The backend is based on [scala-pet-store](https://github.com/pauljamescleary/scala-pet-store).
The frontend is based on [Reason](https://reasonml.github.io)'s QuickStart, which I normally don't use (I use [Reason Scripts](https://github.com/reasonml-old/reason-scripts/blob/master/README.md)).

Scala has an amazing backend ecosystem, while Reason is amazing on the frontend, which is why I chose them for their respective roles.
Reason is not yet mature enough to attract beginners for its backend usage. However, for the frontend, it feels much more natural to use than Scala.js for a JavaScript programmer like me.

[Frontend Src](https://github.com/cnguy/strawpoll/tree/master/src/main/resources/frontend)

## Architecture

Backend:
* http4s, cats, doobie, circe

Frontend:
* reason-reroute

Strategies:
* short-polling for now

## Developing

Shell 1:
```sh
sbt
# Shell will pop up, and these commands will be very useful:
# reload on save: reStart
# lint: scalafmt
```

Shell 2:
```sh
yarn && yarn start # npm install && npm run start
```

Shell 3:
```sh
yarn webpack # npm run webpack
```

And then go to [localhost](http://localhost:8080).
