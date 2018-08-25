open Types;

let answerStub = (pollId: int, answerStub: answerStub) =>
  Json.Encode.(
    object_([
      ("pollId", int(pollId)),
      ("rank", int(answerStub.fieldId)),
      ("response", string(answerStub.response)),
      ("count", int(0)),
    ])
  );

let answerStubs = (pollId: int, answerStubs: list(answerStub)) =>
  answerStubs |> Json.Encode.list(answerStub(pollId));