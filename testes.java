import java.io.RandomAccessFile;

import arvore.ArvoreBMais;
import hash.HashExtensivel;

public class testes {
    public static void main(String[] args) {
        try{
            FileManager fm = new FileManager();
            fm.loadFile("data.dat");
            HashExtensivel he = fm.createHashExtensivel(1, 50);
            System.out.println(fm.findProdutoUsingHash(he, 2323));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
