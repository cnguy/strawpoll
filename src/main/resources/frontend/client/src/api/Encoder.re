open Types;

let answerStub = (answerStub: answerStub) =>
  Json.Encode.(
    object_([
      ("rank", int(answerStub.fieldId)),
      ("response", string(answerStub.response)),
      ("count", int(0)),
    ])
  );

let answerStubs = (answerStubs: list(answerStub)) =>
  answerStubs |> Json.Encode.list(answerStub);