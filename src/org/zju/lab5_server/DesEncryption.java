package org.zju.lab5_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;


class DesEncryption
{
int PLAIN_FILE_OPEN_ERROR =  -1;   
int KEY_FILE_OPEN_ERROR = -2;   
int CIPHER_FILE_OPEN_ERROR = -3;   
int OK = 1;      

int IP_Table[] = new int[] {  57,49,41,33,25,17,9,1,
                                 59,51,43,35,27,19,11,3,
                                 61,53,45,37,29,21,13,5,
                                 63,55,47,39,31,23,15,7,
                                 56,48,40,32,24,16,8,0,
                                 58,50,42,34,26,18,10,2,
                                 60,52,44,36,28,20,12,4,
                                 62,54,46,38,30,22,14,6}; 
//Äæ³õÊ¼ÖÃ»»±íIP^-1
int IP_1_Table[] = new int [] {39,7,47,15,55,23,63,31,
		   38,6,46,14,54,22,62,30,
		   37,5,45,13,53,21,61,29,
		   36,4,44,12,52,20,60,28,
		   35,3,43,11,51,19,59,27,
		   34,2,42,10,50,18,58,26,
		   33,1,41,9,49,17,57,25,
		   32,0,40,8,48,16,56,24};

//À©³äÖÃ»»±íE
int E_Table[] = new int [] {31, 0, 1, 2, 3, 4,
	              3,  4, 5, 6, 7, 8,
	              7,  8,9,10,11,12,
	              11,12,13,14,15,16,
	              15,16,17,18,19,20,
	              19,20,21,22,23,24,
	              23,24,25,26,27,28,
	              27,28,29,30,31, 0};

//ÖÃ»»º¯ÊýP
int P_Table[] = new int [] {15,6,19,20,28,11,27,16,
				  0,14,22,25,4,17,30,9,
				  1,7,23,13,31,26,2,8,
				  18,12,29,5,21,10,3,24};

//SºÐ
int S[][][] = new int [][][]//S1
 	         {{{14,4,13,1,2,15,11,8,3,10,6,12,5,9,0,7},
   	         {0,15,7,4,14,2,13,1,10,6,12,11,9,5,3,8},
  	          {4,1,14,8,13,6,2,11,15,12,9,7,3,10,5,0},
  	          {15,12,8,2,4,9,1,7,5,11,3,14,10,0,6,13}},
  	          //S2
 	           {{15,1,8,14,6,11,3,4,9,7,2,13,12,0,5,10},
 	           {3,13,4,7,15,2,8,14,12,0,1,10,6,9,11,5},
 	           {0,14,7,11,10,4,13,1,5,8,12,6,9,3,2,15},
 	           {13,8,10,1,3,15,4,2,11,6,7,12,0,5,14,9}},
 	           //S3
 	           {{10,0,9,14,6,3,15,5,1,13,12,7,11,4,2,8},
 	           {13,7,0,9,3,4,6,10,2,8,5,14,12,11,15,1},
   	           {13,6,4,9,8,15,3,0,11,1,2,12,5,10,14,7},
 	           {1,10,13,0,6,9,8,7,4,15,14,3,11,5,2,12}},
 	           //S4
 	           {{7,13,14,3,0,6,9,10,1,2,8,5,11,12,4,15},
 	           {13,8,11,5,6,15,0,3,4,7,2,12,1,10,14,9},
 	           {10,6,9,0,12,11,7,13,15,1,3,14,5,2,8,4},
 	           {3,15,0,6,10,1,13,8,9,4,5,11,12,7,2,14}},
 	           //S5
 	           {{2,12,4,1,7,10,11,6,8,5,3,15,13,0,14,9},
 	           {14,11,2,12,4,7,13,1,5,0,15,10,3,9,8,6},
 	           {4,2,1,11,10,13,7,8,15,9,12,5,6,3,0,14},
 	           {11,8,12,7,1,14,2,13,6,15,0,9,10,4,5,3}},
 	           //S6
 	           {{12,1,10,15,9,2,6,8,0,13,3,4,14,7,5,11},
 	           {10,15,4,2,7,12,9,5,6,1,13,14,0,11,3,8},
 	           {9,14,15,5,2,8,12,3,7,0,4,10,1,13,11,6},
 	           {4,3,2,12,9,5,15,10,11,14,1,7,6,0,8,13}},
 	           //S7
 	           {{4,11,2,14,15,0,8,13,3,12,9,7,5,10,6,1},
 	           {13,0,11,7,4,9,1,10,14,3,5,12,2,15,8,6},
 	           {1,4,11,13,12,3,7,14,10,15,6,8,0,5,9,2},
 	           {6,11,13,8,1,4,10,7,9,5,0,15,14,2,3,12}},
 	           //S8
 	           {{13,2,8,4,6,15,11,1,10,9,3,14,5,0,12,7},
 	           {1,15,13,8,10,3,7,4,12,5,6,11,0,14,9,2},
 	           {7,11,4,1,9,12,14,2,0,6,10,13,15,3,5,8},
 	           {2,1,14,7,4,10,8,13,15,12,9,0,3,5,6,11}}};
//ÖÃ»»Ñ¡Ôñ1
int PC_1[] = new int [] {56,48,40,32,24,16,8,
	          0,57,49,41,33,25,17,
	          9,1,58,50,42,34,26,
	          18,10,2,59,51,43,35,
	          62,54,46,38,30,22,14,
	          6,61,53,45,37,29,21,
	          13,5,60,52,44,36,28,
	          20,12,4,27,19,11,3};

//ÖÃ»»Ñ¡Ôñ2
int PC_2[] = new int [] {13,16,10,23,0,4,2,27,
	          14,5,20,9,22,18,11,3,
	          25,7,15,6,26,19,12,1,
	          40,51,30,36,46,54,29,39,
	          50,44,32,46,43,48,38,55,
	          33,52,45,41,49,35,28,31};

//¶Ô×óÒÆ´ÎÊýµÄ¹æ¶¨
int MOVE_TIMES[] = new int [] {1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1};   


//×Ö½Ú×ª»»³É¶þ½øÖÆ   
int ByteToBit(byte ch, byte bit[])
 {   
    int cnt;  
   for(cnt = 0;cnt < 8; cnt++)
    {   
	   bit[cnt] = (byte) ((ch>>cnt)&1);   
    }   
    return 0;   
  }   
  
//¶þ½øÖÆ×ª»»³É×Ö½Ú   
byte BitToByte(byte bit[])
{   
    int cnt;   
    byte ch = 0;
    for(cnt = 0;cnt < 8; cnt++)
    {   
        ch |= bit[cnt]<<cnt;   
     }   
    return ch;   
 }   
  
//½«³¤¶ÈÎª8µÄ×Ö·û´®×ªÎª¶þ½øÖÆÎ»´®   
int ByteToBit64(byte ch[], byte bits64[])
{   
    int cnt;   
    for(cnt = 0; cnt < 8; cnt++)
    {     
    	byte bits[] = new byte[8];
    	ByteToBit(ch[cnt], bits);
    	System.arraycopy(bits, 0, bits64, cnt<<3, 8);
    }   
    return 0;   
 }   
  
//½«¶þ½øÖÆÎ»´®×ªÎª³¤¶ÈÎª8µÄ×Ö·û´®   
int Bit64ToByte(byte bit[], byte ch[])
{   
    int cnt;   
    for(cnt = 0; cnt < 8; cnt++)
    {   
    	byte bit8[] = new byte [8];
    	System.arraycopy(bit, cnt<<3, bit8, 0, 8);
    	byte abyte = BitToByte(bit8);
        ch[cnt] = abyte;
    }   
    return 0;   
}   
  
//Éú³É×ÓÃÜÔ¿   
int DES_MakeSubKeys(byte key[],byte subKeys[][])
{   
   byte temp[] = new byte[56];   
   int cnt;   
    DES_PC1_Transform(key,temp);    //PC1ÖÃ»»   
   for(cnt = 0; cnt < 16; cnt++)
     {  //16ÂÖµø´ú£¬²úÉú16¸ö×ÓÃÜÔ¿   
      DES_ROL(temp,MOVE_TIMES[cnt]);//Ñ­»·×óÒÆ   
     DES_PC2_Transform(temp,subKeys[cnt]);//PC2ÖÃ»»£¬²úÉú×ÓÃÜÔ¿   
      }   
    return 0;   
 }   
  
//ÃÜÔ¿ÖÃ»»1   
int DES_PC1_Transform(byte key[], byte tempbts[])
{   
    int cnt;       
    for(cnt = 0; cnt < 56; cnt++)
   {   
        tempbts[cnt] = key[PC_1[cnt]];   
   }   
    return 0;   
}   
  
//ÃÜÔ¿ÖÃ»»2   
int DES_PC2_Transform(byte key[], byte tempbts[])
{   
    int cnt;   
   for(cnt = 0; cnt < 48; cnt++) 
   {   
        tempbts[cnt] = key[PC_2[cnt]];   
    }   
    return 0;   
}   
  
//Ñ­»·×óÒÆ   
int DES_ROL(byte data[], int time)
{      
    byte temp[] = new byte[56];   
 
    //±£´æ½«ÒªÑ­»·ÒÆ¶¯µ½ÓÒ±ßµÄÎ»   
    System.arraycopy(data, 0, temp, 0, time);
    System.arraycopy(data, 28, temp, time, time);
       
   //Ç°28Î»ÒÆ¶¯   

    System.arraycopy(data, time, data, 0, 28-time);

   System.arraycopy(temp, 0, data, 28-time, time);
 
   //ºó28Î»ÒÆ¶¯   
   System.arraycopy(data, 28+time, data, 28, 28 - time);
   System.arraycopy(temp, time, data, 56 - time, time);  
  
    return 0;   
}   
  
//IPÖÃ»»   
int DES_IP_Transform(byte data[])
{   
    int cnt;   
    byte temp[] = new byte[64];   
    for(cnt = 0; cnt < 64; cnt++)
 {   
        temp[cnt] = data[IP_Table[cnt]];   
   }   
    System.arraycopy(temp, 0, data, 0, 64);
    return 0;   
}   
 
//IPÄæÖÃ»»   
int DES_IP_1_Transform(byte data[])
{   
    int cnt;   
    byte temp[] = new byte[64];   
    for(cnt = 0; cnt < 64; cnt++)
{   
        temp[cnt] = data[IP_1_Table[cnt]];   
    }   

    System.arraycopy(temp, 0, data, 0, 64);
    return 0;   
}   
  
//À©Õ¹ÖÃ»»   
int DES_E_Transform(byte data[])
{   
    int cnt;   
    byte temp[] = new byte[48];   
    for(cnt = 0; cnt < 48; cnt++)
     {   
        temp[cnt] = data[E_Table[cnt]];   
    }      
    System.arraycopy(temp, 0, data, 0, 48);
    return 0;   
}   
  
//PÖÃ»»   
int DES_P_Transform(byte data[])
{   
   int cnt;   
    byte temp[] = new byte[32];   
   for(cnt = 0; cnt < 32; cnt++)
{   
        temp[cnt] = data[P_Table[cnt]];   
    }      
   System.arraycopy(temp, 0, data, 0, 32);
    return 0;   
}   
  
//Òì»ò   
int DES_XOR(byte R[], byte L[] ,int count)
{   
    int cnt;   
   for(cnt = 0; cnt < count; cnt++)
{   
       R[cnt] ^= L[cnt];   
    }   
    return 0;   
}   
  
//SºÐÖÃ»»   
int DES_SBOX(byte data[])
{   
   int cnt;   
    int line,row,output;   
    int cur1,cur2;   
    for(cnt = 0; cnt < 8; cnt++)
      {   
        cur1 = cnt*6;   
        cur2 = cnt<<2;   
          
       //¼ÆËãÔÚSºÐÖÐµÄÐÐÓëÁÐ   
        line = (data[cur1]<<1) + data[cur1+5];   
        row = (data[cur1+1]<<3) + (data[cur1+2]<<2) + (data[cur1+3]<<1) + data[cur1+4];   
        output = S[cnt][line][row];   
 
       //»¯Îª2½øÖÆ   
        data[cur2] = (byte) ((output&0X08)>>3);   
        data[cur2+1] = (byte) ((output&0X04)>>2);   
        data[cur2+2] = (byte) ((output&0X02)>>1);   
        data[cur2+3] = (byte) (output&0x01);   
      }      
    return 0;   
}   
  
//½»»»   
int DES_Swap(byte left[], int leftPos, byte right[], int rightPos)
  {   
    byte temp[] = new byte[32];   
    System.arraycopy(left, leftPos, temp, 0, 32); 
    System.arraycopy(right, rightPos, left, leftPos, 32);
    System.arraycopy(temp, 0, right, rightPos, 32);
    return 0;   
 }   
 
//¼ÓÃÜµ¥¸ö·Ö×é   
int DES_EncryptBlock(byte plainBlock[], byte subKeys[][], byte cipherBlock[])
{   
    byte plainBits[] = new byte[64];   
    byte copyRight[] = new byte[48];   
    int cnt;   
  
    ByteToBit64(plainBlock,plainBits);        
    //³õÊ¼ÖÃ»»£¨IPÖÃ»»£©   
    DES_IP_Transform(plainBits);   
 
    //16ÂÖµü´ú   
    for(cnt = 0; cnt < 16; cnt++)
      {          
    	System.arraycopy(plainBits, 32, copyRight, 0, 32);
        //½«ÓÒ°ë²¿·Ö½øÐÐÀ©Õ¹ÖÃ»»£¬´Ó32Î»À©Õ¹µ½48Î»   
       DES_E_Transform(copyRight);   
        //½«ÓÒ°ë²¿·ÖÓë×ÓÃÜÔ¿½øÐÐÒì»ò²Ù×÷   
        DES_XOR(copyRight,subKeys[cnt],48);    
        //Òì»ò½á¹û½øÈëSºÐ£¬Êä³ö32Î»½á¹û   
        DES_SBOX(copyRight);   
        //PÖÃ»»   
        DES_P_Transform(copyRight);   
        //½«Ã÷ÎÄ×ó°ë²¿·ÖÓëÓÒ°ë²¿·Ö½øÐÐÒì»ò   
        DES_XOR(plainBits,copyRight,32);   
        if(cnt != 15)
          {   
            DES_Swap(plainBits, 0 ,plainBits, 32);   
           }   
      }   
    //Äæ³õÊ¼ÖÃ»»£¨IP^1ÖÃ»»£©   
    DES_IP_1_Transform(plainBits);   
    Bit64ToByte(plainBits,cipherBlock);   
    return 0;   
}   
  
//½âÃÜµ¥¸ö·Ö×é   
int DES_DecryptBlock(byte cipherBlock[], byte subKeys[][],byte plainBlock[])
{   
    byte cipherBits[] = new byte[64];   
    byte copyRight[] = new byte[48];   
    int cnt;   
  
    ByteToBit64(cipherBlock,cipherBits);          
    //³õÊ¼ÖÃ»»£¨IPÖÃ»»£©   
    DES_IP_Transform(cipherBits);   
       
   //16ÂÖµü´ú   
    for(cnt = 15; cnt >= 0; cnt--)
      {         
    	
    	System.arraycopy(cipherBits, 32, copyRight, 0, 32);
        //½«ÓÒ°ë²¿·Ö½øÐÐÀ©Õ¹ÖÃ»»£¬´Ó32Î»À©Õ¹µ½48Î»   
        DES_E_Transform(copyRight);   
        //½«ÓÒ°ë²¿·ÖÓë×ÓÃÜÔ¿½øÐÐÒì»ò²Ù×÷   
        DES_XOR(copyRight,subKeys[cnt],48);        
        //Òì»ò½á¹û½øÈëSºÐ£¬Êä³ö32Î»½á¹û   
        DES_SBOX(copyRight);   
        //PÖÃ»»   
        DES_P_Transform(copyRight);        
        //½«Ã÷ÎÄ×ó°ë²¿·ÖÓëÓÒ°ë²¿·Ö½øÐÐÒì»ò   
        DES_XOR(cipherBits,copyRight,32);   
        if(cnt != 0)
          {   
            //×îÖÕÍê³É×óÓÒ²¿µÄ½»»»   
            DES_Swap(cipherBits, 0 ,cipherBits, 32);   
          }   
       }   
    
  //Äæ³õÊ¼ÖÃ»»£¨IP^1ÖÃ»»£©   
    DES_IP_1_Transform(cipherBits);   
    Bit64ToByte(cipherBits,plainBlock);   
    return 0;   
   }   
  
//¼ÓÃÜÎÄ¼þ   
byte[] DES_Encrypt(byte[] in, String keyStr) throws IOException
{    
    int count;   
    int fileLen = in.length;
    int outFileLen = fileLen / 8;
    outFileLen *= 8;
    if(fileLen % 8 > 0)
    	outFileLen += 8;
    byte[] out = new byte[outFileLen];
    byte plainBlock[] = new byte[8],cipherBlock[] = new byte[8],keyBlock[] = new byte[8];   
    byte bKey[] = new byte[64];   
    byte subKeys[][] = new byte[16][48];   
    
    //ÉèÖÃÃÜÔ¿      
    //½«ÃÜÔ¿×ª»»Îª¶þ½øÖÆÁ÷   
    
    for(int i = 0; i < keyStr.length() && i < keyBlock.length; i++)
    {
    	keyBlock[i] = (byte) keyStr.toCharArray()[i];
    }

    
    ByteToBit64(keyBlock,bKey);   
    //Éú³É×ÓÃÜÔ¿   
    DES_MakeSubKeys(bKey,subKeys);   
    int inPos = 0;
    while(true)
    {   

    	if(fileLen <= inPos)
    		break;
    	int copyNum = 8;
    	if(fileLen - inPos < 8)
    		copyNum = (int) (fileLen - inPos);
    	System.arraycopy(in, inPos, plainBlock, 0, copyNum);
        //Ã¿´Î¶Á8¸ö×Ö½Ú£¬²¢·µ»Ø³É¹¦¶ÁÈ¡µÄ×Ö½ÚÊý   
        if(copyNum == 8)
         {   
            DES_EncryptBlock(plainBlock,subKeys,cipherBlock);   
        	System.arraycopy(cipherBlock, 0, out, inPos, 8);
            
        }   
       
    else
      {   
        //Ìî³ä  
    	byte newPlainBlock [] = new byte[8];
    	System.arraycopy(plainBlock, 0, newPlainBlock, 0, copyNum);
        //×îºóÒ»¸ö×Ö·û±£´æ°üÀ¨×îºóÒ»¸ö×Ö·ûÔÚÄÚµÄËùÌî³äµÄ×Ö·ûÊýÁ¿   
    	newPlainBlock[7] = (byte) (8 - copyNum);   
        DES_EncryptBlock(newPlainBlock,subKeys,cipherBlock);   
    	System.arraycopy(cipherBlock, 0, out, inPos, 8);
    }  
        inPos += 8;
        }
    return out;   
 }   
  
//½âÃÜÎÄ¼þ   
byte[] DES_Decrypt(byte[] in, String keyStr) throws IOException{   
    int count = 0,times = 0;   
    long fileLen = in.length;   
    byte[] out = new byte[(int) fileLen];
    byte plainBlock[] = new byte[8],cipherBlock[] = new byte[8],keyBlock[] = new byte[8];   
    byte bKey[]  = new byte[64];   
    byte subKeys[][] = new byte[16][48];   
  
    //ÉèÖÃÃÜÔ¿   
    //½«ÃÜÔ¿×ª»»Îª¶þ½øÖÆÁ÷   

    for(int i = 0; i < keyStr.length() && i < keyBlock.length; i++)
    {
    	keyBlock[i] = (byte) keyStr.toCharArray()[i];
    }
    ByteToBit64(keyBlock,bKey);   
    //Éú³É×ÓÃÜÔ¿   
    DES_MakeSubKeys(bKey,subKeys);   
  
    //È¡ÎÄ¼þ³¤¶È   
    int pos = 0;
    while(true)
   {   
        //ÃÜÎÄµÄ×Ö½ÚÊýÒ»¶¨ÊÇ8µÄÕûÊý±¶   
    	int copyNum = 8;
    	if(fileLen - pos < 8)
    		copyNum = (int) (fileLen - pos);
    	System.arraycopy(in, pos, cipherBlock, 0, copyNum);
        DES_DecryptBlock(cipherBlock,subKeys,plainBlock);      
        times += 8;
        if(times < fileLen)
        {   
        	System.arraycopy(plainBlock, 0, out, pos, 8);
        	pos += 8;
        }   
        else
        {   
            break;   
        }   
    }   
    //ÅÐ¶ÏÄ©Î²ÊÇ·ñ±»Ìî³ä   
    if(plainBlock[7] < 8)
   {   
        for(count = 8 - plainBlock[7]; count < 7; count++)
        {   
            if(plainBlock[count] != 0)
           {   
                break;   
            }   
        }   
    }      
    if(count == 7){//ÓÐÌî³ä   
    	System.arraycopy(plainBlock, 0, out, pos, 8 - plainBlock[7]);
    }   
    else{//ÎÞÌî³ä   
    	System.arraycopy(plainBlock, 0, out, pos, 8);
    }   
  
    return out;   
} 
	
	byte[] ToBytes(String s)
	{
		return CharsToBytes(s.toCharArray());
	}
	
	byte[] CharsToBytes(char[] chars)
	{
		
		byte[] bytes = new byte[chars.length * 2];
		for(int i = 0; i < chars.length; i++)
		{
			bytes[i * 2] = (byte) chars[i];
			bytes[i * 2 + 1] = (byte) (chars[i] >> 8);
		}
		return bytes;
	}
	
	String BytesToString(byte[] bytes)
	{
		return new String(BytesToChars(bytes));
	}
	
	char[] BytesToChars(byte[] bytes)
	{
		char[] ch = new char[bytes.length / 2];
		for(int i = 0; 2 * i < bytes.length / 2; i++)
		{
			if(2 * i + 1 < bytes.length)
				ch[i] = (char) ((char) (bytes[2 * i] & 0xff) | ((char) bytes[2 * i + 1]) << 8);
			else
				ch[i] = (char) bytes[2 * i];
		}
		return ch;
	}

}