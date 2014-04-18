package Json;

public class JoinGameServletResponse {

	public String status;
	public String msg;
	
	public JoinGameServletResponse(String statusIn, String msgIn) {
		this.status = statusIn;
		this.msg = msgIn;
	}
}
