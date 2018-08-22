type rawAnswer = {
  id: int,
  pollId: int,
  response: string,
  count: int,
};

type rawPoll = {
  id: int,
  question: string,
};

type answer = {
  id: int,
  response: string,
  count: int,
};

type answerStub = {
  fieldId: int,
  response: string,
};

type poll = {
  id: int,
  question: string,
  answers: list(answer),
};