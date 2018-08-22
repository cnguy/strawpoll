open Types;

type state = {
  question: string,
  answers: list(answerStub),
};

type action =
  | SetQuestion(string)
  | ChangeAnswer(int, string)
  | AddAnswer(answerStub)
  | RemoveAnswer(int)
  | SubmitPoll;

let component = ReasonReact.reducerComponent("NewPoll");

let make = _children => {
  ...component,
  initialState: () => {
    question: "",
    answers: [{fieldId: 0, response: ""}, {fieldId: 1, response: ""}],
  },
  reducer: (action, state) =>
    switch (action) {
    | SetQuestion(text) => ReasonReact.Update({...state, question: text})
    | ChangeAnswer(fieldId, text) =>
      let result =
        state.answers
        |> List.map((answer: answerStub) =>
             answer.fieldId === fieldId ? {fieldId, response: text} : answer
           );
      ReasonReact.Update({...state, answers: result});
    | AddAnswer(answer) => NoUpdate
    | RemoveAnswer(answerId) => NoUpdate
    | SubmitPoll =>
      Js.log(state);
      PollService.makePoll({question: state.question}, state.answers);
      NoUpdate;
    },
  render: self => {
    let answerFields =
      self.state.answers
      |> List.map(answer =>
           <input
             key={string_of_int(answer.fieldId)}
             onChange={
               event =>
                 self.send(
                   ChangeAnswer(
                     answer.fieldId,
                     event->ReactEvent.Form.target##value,
                   ),
                 )
             }
             value={answer.response}
             placeholder="Enter Poll Option"
           />
         )
      |> Array.of_list
      |> ReasonReact.array;
    <>
      <input
        onChange={
          event =>
            self.send(SetQuestion(event->ReactEvent.Form.target##value))
        }
        value={self.state.question}
        placeholder="Type your question here"
      />
      answerFields
      <button onClick={_event => self.send(SubmitPoll)}>
        {ReasonReact.string("Submit")}
      </button>
    </>;
  },
};