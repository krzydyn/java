/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package algebra;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sys.Log;
import text.Text;

public class TuringMach {
	static class State {
		final String name;
		public State(String n) {name=n;}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public boolean equals(Object o) {
			State s = (State)o;
			return name.equals(s.name);
		}
	}
	static class StateKey {
		State state;
		String inp;
		@Override
		public int hashCode() {
			return state.hashCode()+inp.hashCode()*31;
		}
		@Override
		public boolean equals(Object o) {
			StateKey sk = (StateKey)o;
			return state.equals(sk.state) && inp.equals(sk.inp);
		}
	}
	static class Action {
		String out;   // write
		int mv;       // move (-1(left), 0(no) or 1(right)
		State next;   // next state
	}

	private final Map<String,State> states = new HashMap<>();
	private final Map<StateKey,Action> rules = new HashMap<>();
	private State currentState=null;

	private State mkState(String name) {
		State state = states.get(name);
		if (state == null) {
			state = new State(name);
			states.put(name, state);
		}
		return state;
	}

	public void loadMachine(Reader rd) {
		//BufferedReader br = new BufferedReader(rd);
		String pattern = "[\\s,]+";
		Scanner sc = new Scanner(rd);
		sc.useDelimiter(pattern);

		String tok;
		while (sc.hasNext()) {
			StateKey sk = new StateKey();
			tok=sc.next();
			sk.state = mkState(tok);
			sk.inp = sc.next();
			Action a = new Action();
			a.out=sc.next();
			tok=sc.next();
			a.mv=tok.equals("L") ? -1 : tok.equals("R") ? 1 : 0;
			tok = sc.next();
			a.next = mkState(tok);
			rules.put(sk,a);
		}
		sc.close();
	}
	public void loadMachine(String s) {
		loadMachine(new StringReader(s));
	}

	public String run(String initState, String tape) {
		List<String> t = new ArrayList<>();
		for (String s : tape.split("[,\\s]+"))
			t.add(s);

		t=run(initState, t);
		return Text.join(",", t).replaceFirst("(#,)+", "");
	}
	public List<String> run(String initState, List<String> tape) {
		currentState = mkState(initState);
		int idx=0;
		StateKey key = new StateKey();
		while (!currentState.name.equals("T")) {
			String inp = tape.get(idx);
			key.inp = inp;
			key.state = currentState;
			Action a = rules.get(key);
			if (a == null) {
				Log.error("undefined input '%s' in state '%s'", key.inp, key.state.name);
				break;
			}
			//Log.debug("tape: %s",Text.join("", tape));
			//Log.debug("      %s^   write %s",Text.repeat(" ", idx),a.out);
			tape.set(idx, a.out);
			idx += a.mv;
			if (idx < 0) {
				tape.add(0, "#");
				idx = 0;
			} else if (idx >= tape.size()) {
				tape.add("#");
				idx = tape.size()-1;
			}

			currentState = a.next;
		}
		return tape;
	}
}
