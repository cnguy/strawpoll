open Types;

let rawAnswersToAnswers = (rawAnswers: list(rawAnswer)) => {
  let answers =
    rawAnswers
    |> List.map(({id, response, count}: rawAnswer) => {id, response, count});
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