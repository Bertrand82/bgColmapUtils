package bg.process.log;

public class LogData {

	String name;
	String value;
	String ss;
	public LogData(String variableName, String variableValue, String sss) {
		this.name=variableName;
		this.value = variableValue;
		this.ss=sss;
	}
	@Override
	public String toString() {
		return "Data : [name:" + name + ", value:" + value +"]";
	}


	public String toStringVerbose() {
		return "Data : [name:" + name + ", value:" + value +"   string: "+ss+ "]";
	}
	public boolean isPertinent() {
		if (name.equals("process")) {
			return false;
		}
		return true;
	}

	
}
