open Types;

let answerStub = (answerStub: answerStub) =>
  Json.Encode.(
    object_([
      ("response", string(answerStub.response)),
      ("rank", int(answerStub.fieldId)),
      ("count", int(0)),
    ])
  );

let answerStubs = (answerStubs: list(answerStub)) =>
  answerStubs |> Json.Encode.list(answerStub);

let answer = (answer: rawAnswer) =>
  Json.Encode.(
    object_([
      ("pollId", int(answer.pollId)),
      ("response", string(answer.response)),
      ("rank", int(answer.rank)),
      ("count", int(0)),
    ])
  );

let answers = (answers: list(rawAnswer)): Js.Json.t =>
  answers |> Json.Encode.list(answer);