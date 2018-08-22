open Types;

type state = {
  question: string,
  answers: list(answer),
};

type action =
  | SetQuestion(string)
  | AddAnswer(answer)
  | RemoveAnswer(int);

let component = ReasonReact.reducerComponent("NewPoll");

let make = _children => {
  ...component,
  initialState: () => {question: "", answers: []},
  reducer: (action, state) =>
    switch (action) {
    | SetQuestion(text) => ReasonReact.Update({...state, question: text})
    | AddAnswer(answer) => NoUpdate
    | RemoveAnswer(answerId) => NoUpdate
    },
  render: self =>
    <div>
      {ReasonReact.string("I love variant types and pattern-matching!")}
    </div>,
};