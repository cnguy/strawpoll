type rawAnswer = {
  id: int,
  pollId: int,
  response: string,
  rank: int,
  count: int,
};

type rawPoll = {
  id: int,
  question: string,
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
  answers: list(answer),
};

type answerStub = {
  fieldId: int,
  response: string,
};

type pollStub = {question: string};