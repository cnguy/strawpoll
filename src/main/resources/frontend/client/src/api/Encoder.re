open Types;

let answerStub = (answerStub: answerStub) =>
  Json.Encode.(
    object_([
      ("pollId", int(0)),
      ("response", string(answerStub.response)),
      ("rank", int(answerStub.fieldId)),
      ("count", int(0)),
    ])
  );

let answerStubs = (answerStubs: list(answerStub)) =>
  answerStubs |> Json.Encode.list(answerStub);