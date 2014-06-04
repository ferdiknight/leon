/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author ferdinand
 */
public class NewClass
{
    public static void main(String[] args)
    {
        Calendar c = Calendar.getInstance();
        c.set(2012, 11, 1);
        System.out.println(c.getTimeInMillis());
        System.out.println(System.currentTimeMillis());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String s = sdf.format(new Date(1358837523041l));
    }
}
