open Types;

let answer = json =>
  Json.Decode.{
    id: json |> field("id", int),
    pollId: json |> field("pollId", int),
    response: json |> field("response", string),
    rank: json |> field("rank", int),
    count: json |> field("count", int),
  };

let answers = json => json |> Json.Decode.list(answer);

let poll = json =>
  Json.Decode.{
    id: json |> field("id", int),
    question: json |> field("question", string),
    securityType:
      json
      |> field("securityType", Json.Decode.optional(string))
      |> PollSecurityType.fromOptionalString,
  };