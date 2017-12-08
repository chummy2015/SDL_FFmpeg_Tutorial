
// Otsu Thresholder Demo
// A.Greensted
// http://www.labbookpages.co.uk
// Please use however you like. I'd be happy to hear any feedback or comments.

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class OtsuDemo
{
	public OtsuDemo(String filename)
	{
		// Load Source image
		BufferedImage srcImage = null;
		//Bit24_to_256Index(filename);
		try
		{
			File imgFile = new File(filename);
			srcImage = javax.imageio.ImageIO.read(imgFile);
		}
		catch (IOException ioE)
		{
			System.err.println(ioE);
			System.exit(1);
		}

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();

		// Get raw image data
		Raster raster = srcImage.getData();
		DataBuffer buffer = raster.getDataBuffer();

		int type = buffer.getDataType();
		if (type != DataBuffer.TYPE_BYTE)
		{
			System.err.println("Wrong image data type");
			System.exit(1);
		}
		if (buffer.getNumBanks() != 1)
		{
			System.err.println("Wrong image data format");
			System.exit(1);
		}

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);
		//srcData�Ǵ洢�Ҷ�ͼ����ֽ�����
		// Sanity check image
		if (width * height  != srcData.length) {
			System.err.println("Unexpected image data size. Ӧ��Ϊ�Ҷ�ͼ��");
			System.exit(1);
		}

		// Output Image info
		System.out.printf("Loaded image: '%s', width: %d, height: %d, num bytes: %d\n", filename, width, height, srcData.length);

		byte[] dstData = new byte[srcData.length];

		// Create Otsu Thresholder������ֵ��
		OtsuThresholder thresholder = new OtsuThresholder();
		int threshold = thresholder.doThreshold(srcData, dstData);

		System.out.printf("��ֵ: %d\n", threshold);

		// Create GUI
		GreyFrame srcFrame = new GreyFrame(width, height, srcData);
		GreyFrame dstFrame = new GreyFrame(width, height, dstData);
		GreyFrame histFrame = createHistogramFrame(thresholder);

		JPanel infoPanel = new JPanel();
		infoPanel.add(histFrame);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
		panel.add(infoPanel, BorderLayout.NORTH);
		panel.add(srcFrame, BorderLayout.WEST);
		panel.add(dstFrame, BorderLayout.EAST);
		panel.add(new JLabel("A.Greensted - http://www.labbookpages.co.uk", JLabel.CENTER), BorderLayout.SOUTH);

		JFrame frame = new JFrame("Blob Detection Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);

		// Save Images
		try
		{
			int dotPos = filename.lastIndexOf(".");
			String basename = filename.substring(0,dotPos);

			javax.imageio.ImageIO.write(dstFrame.getBufferImage(), "PNG", new File(basename+"_BW.png"));
			javax.imageio.ImageIO.write(histFrame.getBufferImage(), "PNG", new File(basename+"_hist.png"));
		}
		catch (IOException ioE)
		{
			System.err.println("Could not write image " + filename);
		}
	}

	private GreyFrame createHistogramFrame(OtsuThresholder thresholder)
	{
		int numPixels = 256 * 100;
		byte[] histPlotData = new byte[numPixels];

		int[] histData = thresholder.getHistData();
		int max = thresholder.getMaxLevelValue();
		int threshold = thresholder.getThreshold();

		for (int l=0 ; l<256 ; l++)
		{
			int ptr = (numPixels - 256) + l;
			int val = (100 * histData[l]) / max;

			if (l == threshold)
			{
				for (int i=0 ; i<100 ; i++, ptr-=256) histPlotData[ptr] = (byte) 128;//��ɫ
			}
			else
			{
				for (int i=0 ; i<100 ; i++, ptr-=256) histPlotData[ptr] = (val < i) ? (byte) 255 : 0;
			}
		}

		return new GreyFrame(256, 100, histPlotData);
	}

	public static void main(String args[])
	{
		System.out.println("Otsu Thresholder Demo - A.Greensted - http://www.labbookpages.co.uk");
		if (args.length<1) {
			System.err.println("Provide image filename");
			System.exit(1);
		}

		new OtsuDemo(args[0]);
	}
	private void Bit24_to_256Index(String fileString) {  
		BufferedImage inputImage=null;
       // ��ͼ���ļ����������ݱ�����Byte����  
       try  {  
    	   inputImage = javax.imageio.ImageIO.read(new File(fileString));  
       }  
       catch (IOException e1) {  
           e1.printStackTrace();  
       }  
       int width = inputImage.getWidth();  
       int height= inputImage.getHeight();  
        
       ByteArrayOutputStream bos = new ByteArrayOutputStream( width*height*4 + 54);  
               
       try   {  
           ImageIO.write( inputImage, "BMP", bos);  
       } catch (IOException e)      {  
           e.printStackTrace();  
       }  
       byte[] bSrcfile = bos.toByteArray();    
       // ���ļ��ĳ���(b)=���ݲ���+��ɫ��(1024)+λͼ��Ϣͷ+λͼ�ļ�ͷ  
       byte[] bDestfile = new byte[ width*height+1078 ];               
       // ��ʼ�����ֽ�����  
       bDestfile[0] = bSrcfile[0];  // 00H : 42H  
       bDestfile[1] = bSrcfile[1];  // 01H : 4DH  
       // �ļ���С(B)  
       int fileLength = width * height + 1078 ;  
       byte[] btLen = int2bytes(fileLength);  
       switch( btLen.length )  {  
       case 1:  
           bDestfile[2] = btLen[0];  
           break;  
       case 2:  
           bDestfile[3] = btLen[0];  
           bDestfile[2] = btLen[1];  
           break;  
       case 3:  
           bDestfile[4] = btLen[0];  
           bDestfile[3] = btLen[1];  
           bDestfile[2] = btLen[2];  
           break;  
       case 4:  
           bDestfile[5] = btLen[0];  
           bDestfile[4] = btLen[1];  
           bDestfile[3] = btLen[2];  
           bDestfile[2] = btLen[3];  
       }  
       // ���ݵ�ƫ�Ƶ�ַ�̶�Ϊ1078(436H)  
       bDestfile[10] = 54;   // 36H  
       bDestfile[11] = 4;    // 04H  
       for( int i = 14; i <= 27; i++ )  {  
           bDestfile[i] = bSrcfile[i];    
       }  
       bDestfile[28] = 8;  // 2^8 = 256  
       // ���ݴ�С�ֶ�  
       int biSizeImage = width * height;  // ��256ɫͼ���������ݲ��ֵĴ�СΪ��*��  
       byte[] btSI = int2bytes(biSizeImage);  
       switch( btSI.length )  {  
       case 1:  
           bDestfile[34] = btSI[0];  
           break;  
       case 2:  
           bDestfile[35] = btSI[0];  
           bDestfile[34] = btSI[1];  
           break;  
       case 3:  
           bDestfile[36] = btSI[0];  
           bDestfile[35] = btSI[1];  
           bDestfile[34] = btSI[2];  
           break;  
       case 4:  
           bDestfile[37] = btSI[0];  
           bDestfile[36] = btSI[1];  
           bDestfile[35] = btSI[2];  
           bDestfile[34] = btSI[3];  
       }  
       for( int i = 38; i <= 53; i++ )  {  
           bDestfile[i] = bSrcfile[i];  
       }  
       byte bRGB = 0;  
       // д��ɫ��  36H(54) --> 435H(1077)  
       for( int i = 54; i <= 1077; i += 4, bRGB ++ )   {  
           bDestfile[i] = bRGB;  
           bDestfile[i+1] = bRGB;  
           bDestfile[i+2] = bRGB;  
           bDestfile[i+3] = 0;   // rgbReserved, ����ֵΪ��  
       }       
       // ת��ͼ�����ݲ���  
       for( int i = 1078, j = 54; i < bDestfile.length; i++, j += 3 )  {  
           bDestfile[i] = bSrcfile[j];  
       }  
       BufferedImage outputImage = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_GRAY);  
       ByteArrayInputStream in = new ByteArrayInputStream( bDestfile );    //��b��Ϊ��������  
       try {  
           outputImage = ImageIO.read( in );  
       } catch (IOException e) {  
           e.printStackTrace();  
           }  
       try  {  
           ImageIO.write( outputImage, "BMP",new   File( fileString ));  
       }  
       catch(Exception   ex)  {  
          ex.printStackTrace();  
       }  
    }  
	static byte[] int2bytes(int num)  {  
	   byte[] b=new byte[4];  
	   int mask=0xff;  
	   for(int i=0;i<4;i++)  {  
	       b[i]=(byte)(num>>>(24-i*8));  
	   }  
	   return b;  
	}  
}
