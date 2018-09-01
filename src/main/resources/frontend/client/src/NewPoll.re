open Types;

type state = {
  question: string,
  answerStubs: list(answerStub),
  isLoading: bool,
};

type action =
  | SetQuestion(string)
  | ChangeAnswer(int, string)
  | AddAnswer(answerStub)
  | RemoveAnswer(int)
  | SetIsLoading(bool)
  | SubmitPoll;

let component = ReasonReact.reducerComponent("NewPoll");

let make = _children => {
  ...component,
  initialState: () => {
    question: "",
    answerStubs: [{fieldId: 0, response: ""}, {fieldId: 1, response: ""}],
    isLoading: false,
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
    | SetIsLoading(isLoading) => ReasonReact.Update({...state, isLoading})
    | SubmitPoll =>
      ReasonReact.UpdateWithSideEffects(
        {...state, isLoading: true},
        (
          self =>
            PollService.makePoll(
              {question: state.question},
              state.answerStubs,
            )
            |> Js.Promise.then_(json => {
                 self.send(SetIsLoading(false));
                 let poll = Decoder.poll(json);
                 ReasonReact.Router.push("/p/" ++ string_of_int(poll.id));
                 Js.Promise.resolve();
               })
            |> ignore
        ),
      )
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