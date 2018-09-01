open Types;

type state = {
  question: string,
  answerStubs: list(answerStub),
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
    answerStubs: [{fieldId: 0, response: ""}, {fieldId: 1, response: ""}],
  },
  reducer: (action, state) =>
    switch (action) {
    | SetQuestion(text) => ReasonReact.Update({...state, question: text})
    | ChangeAnswer(fieldId, text) =>
      let result =
        state.answerStubs
        |> List.map((answer: answerStub) =>
             answer.fieldId === fieldId ? {fieldId, response: text} : answer
           );
      let toAppend =
        if (List.length(state.answerStubs) - 1 === fieldId) {
          let toBeAppended = {
            fieldId: List.length(state.answerStubs),
            response: "",
          };
          [toBeAppended];
        } else {
          [];
        };
      ReasonReact.Update({
        ...state,
        answerStubs:
          result
          |> List.append(toAppend)
          |> List.sort((a, b) => a.fieldId - b.fieldId) /* ReasonReact doesn't respect List.append order. */
      });
    | AddAnswer(answer) =>
      ReasonReact.Update({
        ...state,
        answerStubs: state.answerStubs |> List.append([answer]),
      })
    | RemoveAnswer(_answerId) => NoUpdate
    | SubmitPoll =>
      PollService.makePoll({question: state.question}, state.answerStubs)
      |> ignore;
      NoUpdate;
    },
  render: self => {
    let answerFields =
      self.state.answerStubs
      |> List.map(answer =>
           <div key={string_of_int(answer.fieldId)} className="poll__row">
             <input
               type_="text"
               className="poll__row__option"
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
           </div>
         )
      |> Array.of_list
      |> ReasonReact.array;
    <div className="poll">
      <input
        type_="text"
        className="poll__question"
        onChange={
          event =>
            self.send(SetQuestion(event->ReactEvent.Form.target##value))
        }
        value={self.state.question}
        placeholder="Type your question here"
      />
      <br />
      answerFields
      <button onClick={_event => self.send(SubmitPoll)}>
        {ReasonReact.string("Submit")}
      </button>
    </div>;
  },
};