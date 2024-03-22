import java.io.RandomAccessFile;

import arvore.ArvoreBMais;
import hash.HashExtensivel;

public class testes {
    public static void main(String[] args) {
        try{
            FileManager fm = new FileManager();
            fm.loadFile("data.dat");
            //fm.fillHashExtensivel();
            System.out.println(fm.readElement(2323));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
