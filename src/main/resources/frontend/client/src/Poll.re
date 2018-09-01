open Types;

type state = {
  poll: option(poll),
  optionId: int,
  isLoading: bool,
};

type action =
  | SetPoll(option(poll))
  | Vote(int)
  | SetIsLoading(bool)
  | Submit;

let component = ReasonReact.reducerComponent("Poll");

let make = (~id: int, _children) => {
  ...component,
  initialState: () => {poll: None, optionId: 0, isLoading: false},
  didMount: self =>
    PollService.getPoll(id, maybePoll => self.send(SetPoll(maybePoll))),
  reducer: (action, state) =>
    switch (action) {
    | SetPoll(poll) => ReasonReact.Update({...state, poll})
    | Vote((optionId: int)) => ReasonReact.Update({...state, optionId})
    | SetIsLoading(isLoading) => ReasonReact.Update({...state, isLoading})
    | Submit =>
      if (state.optionId !== 0) {
        ReasonReact.UpdateWithSideEffects(
          {...state, isLoading: true},
          (
            self =>
              PollService.vote(state.optionId)
              |> Js.Promise.then_(_json => {
                   self.send(SetIsLoading(false));
                   ReasonReact.Router.push(
                     "/r/"
                     ++ (
                       switch (self.state.poll) {
                       | Some(poll) => string_of_int(poll.id)
                       | None => failwith("Rip")
                       }
                     ),
                   );
                   Js.Promise.resolve();
                 })
              |> ignore
          ),
        );
      } else {
        ReasonReact.UpdateWithSideEffects(state, (_self => ()));
      }
    },
  render: self =>
    switch (self.state.poll) {
    | Some((poll: poll)) =>
      let answers =
        poll.answers
        |> List.sort((a, b) => a.rank - b.rank)
        |> List.map((answer: answer) =>
             <li className="poll__row" key={string_of_int(answer.id)}>
               <input
                 type_="checkbox"
                 onClick=(_event => self.send(Vote(answer.id)))
                 checked={self.state.optionId == answer.id}
               />
               <span className="poll__row__text">
                 {ReasonReact.string(answer.response)}
               </span>
             </li>
           );
      <div className="poll">
        <div className="poll__question">
          {ReasonReact.string(poll.question)}
        </div>
        <ul className="poll__row-container">
          {ReasonReact.array(Array.of_list(answers))}
        </ul>
        <button onClick=(_event => self.send(Submit))>
          {ReasonReact.string("Vote")}
        </button>
      </div>;
    | None => ReasonReact.string("Poll not found.")
    },
};