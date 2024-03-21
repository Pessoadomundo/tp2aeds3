import java.io.RandomAccessFile;

import arvore.ArvoreBMais;

public class testes {
    public static void main(String[] args) {
        try{
            FileManager fm = new FileManager();
            fm.loadFile("data.dat");
            RandomAccessFile penis = new RandomAccessFile("arvore.dat", "rw");
            penis.setLength(0);
            penis.close();
            ArvoreBMais arvore = new ArvoreBMais("arvore.dat", 3);
            fm.resetPosition();
            for(int i=0;i<3;i++){
                long pos = fm.getFilePointerPosition();
                Produto produto = fm.readElement();
                int id = produto.getId();

                System.out.println("Inserindo id: " + id + " pos: " + pos);
                arvore.inserir(id, pos);
            }
            arvore.print();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
