package smartdoor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.maxant.rules.*;

public class RuleEngine {
	private List<Rule> RuleSet;

	public class Person{
		private String PersonName, PersonSurname;
		private int age;
		
		public Person(String name, String surname){
			setPersonName(name);
			setPersonSurname(surname);
			setAge(20);
		}

		public String getPersonSurname() {
			return PersonSurname;
		}

		public void setPersonSurname(String personSurname) {
			PersonSurname = personSurname;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getPersonName() {
			return PersonName;
		}

		public void setPersonName(String personName) {
			PersonName = personName;
		}
		
	}
	
	RuleEngine(){
		Rule Whitelist = new Rule("Whitelist", "input.PersonName==\"Alice\"", "Whitelist", 1, "ch.maxant.produkte", null);
		Rule Surname = new Rule("Surname", "input.PersonSurname==\"Mustermann\"", "Surname", 1, "ch.maxant.produkte", null);
		
		RuleSet = Arrays.asList(Whitelist,Surname);
	}
	
	/**
	 *
	 * 
	 * @param name
	 * @param condition
	 * @return
	 */
	public int addRule(String name,String condition) {
		condition = condition.replaceAll("Person", "input.Person");
		Rule CustomRule = new Rule(name, condition, name, 1, "ch.maxant.produkte", null);
		try {
		ArrayList<Rule> var = new ArrayList<Rule>(RuleSet);
		var.add(CustomRule);
		RuleSet=var;
		}catch(Exception e) {
			System.out.println(e);
		}
		return RuleSet.size();
	}
	

	/**
	 *
	 * 
	 * @param name
	 * @return
	 * @throws DuplicateNameException
	 * @throws CompileException
	 * @throws ParseException
	 * @throws NoMatchingRuleFoundException
	 */
	public String checkRules(String name) throws DuplicateNameException, CompileException, ParseException, NoMatchingRuleFoundException {
		String[] splited = name.split(" ");		
		Engine eng = new Engine(RuleSet, true);
		Person test =new Person(splited[0],splited[1].isEmpty()?null:splited[1]);
		String rs = eng.getBestOutcome(test); 
		
		return rs;
	}
}
