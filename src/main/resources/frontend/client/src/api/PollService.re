open Types;

let rawAnswersToAnswers = (rawAnswers: list(rawAnswer)) => {
  let answers =
    rawAnswers
    |> List.map(({id, rank, response, count}: rawAnswer) =>
         {id, rank, response, count}
       );
  Some(answers);
};

let makePoll = (rawPoll: rawPoll, answers: list(answer)) => {
  let poll = {id: rawPoll.id, question: rawPoll.question, answers};
  Some(poll);
};

let getAnswers = (pollId: int, callback: option(list(answer)) => unit): unit => {
  Js.Promise.(
    Fetch.fetch("/api/polls/" ++ string_of_int(pollId) ++ "/answers")
    |> then_(Fetch.Response.json)
    |> then_(payload =>
         payload
         |> Decoder.answers
         |> rawAnswersToAnswers
         |> callback
         |> resolve
       )
    |> catch(_error => None |> callback |> resolve)
  )
  |> ignore;
  ();
};

let getPoll = (id: int, callback: option(poll) => unit): unit => {
  Js.Promise.(
    Fetch.fetch("/api/polls/" ++ string_of_int(id))
    |> then_(Fetch.Response.json)
    |> then_(payload => {
         let rawPoll = payload |> Decoder.poll;
         getAnswers(
           rawPoll.id,
           answers => {
             switch (answers) {
             | Some(lst) =>
               resolve(callback(makePoll(rawPoll, lst))) |> ignore
             | None => None |> callback |> resolve |> ignore
             };
             ();
           },
         )
         |> resolve;
       })
    |> catch(_error => None |> callback |> resolve)
  )
  |> ignore;
  ();
};

let makeAnswers = (pollId: int, answers: list(answerStub)) => {
  let payload =
    answers
    |> List.filter(answer => String.length(answer.response) > 0)
    |> List.map(stub =>
         {
           pollId,
           response: stub.response,
           rank: stub.fieldId,
           count: 0,
           id: 0,
         }
       )
    |> Encoder.answers;
  Js.Promise.(
    Fetch.fetchWithInit(
      "/api/answers/batch",
      Fetch.RequestInit.make(
        ~method_=Post,
        ~body=Fetch.BodyInit.make(Js.Json.stringify(payload)),
        ~headers=Fetch.HeadersInit.make({"Content-Type": "application/json"}),
        (),
      ),
    )
    |> then_(Fetch.Response.json)
  );
};

let makePoll = (poll: pollStub, answers: list(answerStub)) => {
  let pollPayload = Js.Dict.empty();
  Js.Dict.set(pollPayload, "question", Js.Json.string(poll.question));
  Js.Dict.set(
    pollPayload,
    "answers",
    Encoder.answerStubs(
      answers |> List.filter(answer => String.length(answer.response) > 0),
    ),
  );
  Js.Promise.(
    Fetch.fetchWithInit(
      "/api/polls",
      Fetch.RequestInit.make(
        ~method_=Post,
        ~body=
          Fetch.BodyInit.make(
            Js.Json.stringify(Js.Json.object_(pollPayload)),
          ),
        ~headers=Fetch.HeadersInit.make({"Content-Type": "application/json"}),
        (),
      ),
    )
    |> then_(Fetch.Response.json)
    |> then_(json => {
         let poll = json |> Decoder.poll;
         makeAnswers(poll.id, answers) |> resolve;
       })
  );
};

let vote = (answerId: int) =>
  Js.Promise.(
    Fetch.fetchWithInit(
      "/api/answers/" ++ string_of_int(answerId),
      Fetch.RequestInit.make(
        ~method_=Put,
        ~headers=Fetch.HeadersInit.make({"Content-Type": "application/json"}),
        (),
      ),
    )
    |> then_(Fetch.Response.json)
  );