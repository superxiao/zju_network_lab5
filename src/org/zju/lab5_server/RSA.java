package org.zju.lab5_server;

public class RSA {

	public RSA()
	{
		
	}
	
	public int encoded(int text, int key ,int product )
	{
		int code=1;
		
		key=key+1;		
		while (key!=1)
		{
			code=code*text;
			code=code%product;
			key--;
		}
		return code;
	}

}
