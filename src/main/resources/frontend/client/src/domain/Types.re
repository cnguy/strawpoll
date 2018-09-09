type rawAnswer = {
  id: int,
  pollId: int,
  response: string,
  rank: int,
  count: int,
};

type pollSecurityType =
  | IpAddress
  | BrowserCookie;

module PollSecurityType = {
  let toReadableString = pollSecurityType =>
    switch (pollSecurityType) {
    | Some(IpAddress) => "IP Duplication Checking"
    | Some(BrowserCookie) => "Browser Cookie Duplicate Checking"
    | None => "No Duplication Checking"
    };

  let toString = pollSecurityType =>
    switch (pollSecurityType) {
    | Some(IpAddress) => "IpAddressCheck"
    | Some(BrowserCookie) => "BrowserCookieCheck"
    | None => ""
    };

  let fromString = str =>
    switch (str) {
    | "IpAddressCheck" => Some(IpAddress)
    | "BrowserCookieCheck" => Some(BrowserCookie)
    | _ => None
    };

  let fromOptionalString = optionalStr =>
    switch (optionalStr) {
    | Some(str) => str |> fromString
    | None => None
    };

  let toList = [Some(IpAddress), Some(BrowserCookie), None];
};

type rawPoll = {
  id: int,
  question: string,
  securityType: option(pollSecurityType),
};

type answer = {
  id: int,
  response: string,
  rank: int,
  count: int,
};

type poll = {
  id: int,
  question: string,
  securityType: option(pollSecurityType),
  answers: list(answer),
};

type answerStub = {
  fieldId: int,
  response: string,
};

type pollStub = {
  question: string,
  securityType: option(pollSecurityType),
};