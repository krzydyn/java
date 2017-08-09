package test_SO;

import java.util.Stack;

public class Mart3 {

	int solution(String s) {
		Stack<Integer> stack = new Stack<Integer>();
		for (int i=0; i < s.length(); ++i) {
			char c=s.charAt(i);
			if(c >= '0' && c <= '9') stack.push(c-'0');
			else if (c == '+') {
				if (stack.size() < 2) return -1;
				int a = stack.pop();
				int b = stack.pop();
				stack.push(a+b);
			}
			else if (c == '*') {
				if (stack.size() < 2) return -1;
				int a = stack.pop();
				int b = stack.pop();
				stack.push(a*b);
			}
		}
		if (stack.isEmpty()) return -1;
		return stack.peek();
	}

	public static void main(String[] args) {
		Mart3 m =new Mart3();
		System.out.println(m.solution("13+62*7+*"));
	}
}
