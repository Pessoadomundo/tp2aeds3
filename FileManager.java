import java.io.RandomAccessFile;
import java.util.function.Predicate;

import hash.HashExtensivel; 

public class FileManager {
    private RandomAccessFile raf;

    public FileManager(){
        this.raf = null;
    }

    public FileManager(String path) throws Exception{
        this.start(path);
    }

    /**
     * Cria/abre um novo arquivo, o esvazia e o inicializa.
     */
    public void start(String path) throws Exception{
        this.raf = new RandomAccessFile(path, "rw");
        raf.setLength(0);
        raf.seek(0);
        raf.writeInt(0);
    }

    /**
     * Carrega um arquivo existente e o associa à variável do tipo RAF.
     * @param path
     * @throws Exception
     */
    public void loadFile(String path) throws Exception{
        this.raf = new RandomAccessFile(path, "rw");
    }

    /**
     * Escreve um array de bytes no arquivo na posição atual onde o ponteiro RAF está.
     * @param bArr
     * @throws Exception
     */
    public void writeBytes(byte[] bArr) throws Exception{
        raf.write(bArr);
    }

    /**
     * Lê um array de bytes do arquivo na posição atual onde o ponteiro RAF está.
     * @param len
     * @return byte[] - array de bytes lido
     * @throws Exception
     */
    public byte[] readBytes(int len) throws Exception{
        byte[] bArr = new byte[len];
        raf.read(bArr);
        return bArr;
    }

    /**
     * Lê um elemento (registro) do arquivo na posição atual onde o ponteiro RAF está.
     * @return Produto - registro lido
     * @throws Exception
     */
    public Produto readElement() throws Exception{
        raf.readByte();
        int len = raf.readInt();
        raf.seek(raf.getFilePointer()-5);
        byte[] bArr = new byte[len+5];
        raf.read(bArr);
        Produto p = new Produto(bArr);
        return p;
    }

    /**
     * Posiciona ponteiro RAF para início do arquivo e lê último id inserido para designar o id de um novo registro corretamente (ultimoInserido + 1). Então, uma array de bytes (novo registro/elemento) é então escrita ao ginal do arquivo.
     * @param p
     * @throws Exception
     */
    public int writeElement(Produto p) throws Exception{
        raf.seek(0);
        int lastId = raf.readInt();
        
        p.setId(lastId+1);

        raf.seek(raf.length());
        raf.write(p.toByteArray());

        raf.seek(0);
        raf.writeInt(lastId+1);

        return lastId+1;
    }
    
    /**
     * Move ponteiro RAF para início do arquivo, "pula" último id inserido e procura registro passado por meio de id (parâmetro). Se encontrado, retorna registro (Produto), caso contrário, retorna null.
     * @param id
     * @return Produto - registro lido
     * @throws Exception
     */
    public Produto readElement(int id) throws Exception{
        raf.seek(0);
        raf.readInt();
        while(raf.getFilePointer() < raf.length()){
            Produto p = readElement();
            if(p.getId() == id && p.getAlive()){
                return p;
            }
        }

        return null;
    }

    /**
     * Fecha ponteiro RAF.
     * @throws Exception
     */
    public void close() throws Exception{
        raf.close();
    }

    /**
     * Deleta registro (Produto) do arquivo por meio de id (parâmetro). Retorna true se registro foi deletado, caso contrário, retorna false.
     * @param id
     * @return boolean - true se registro foi deletado, caso contrário, retorna false
     * @throws Exception
     */
    public boolean deleteElement(int id) throws Exception{
        raf.seek(0);
        raf.readInt();
        long pos = 0;
        while(raf.getFilePointer() < raf.length()){
            pos = raf.getFilePointer();
            Produto p = readElement();
            if(p.getId() == id){
                p.setAlive(false);
                raf.seek(pos);
                raf.write(p.toByteArray());
                return true;
            }
        }

        return false;
    }

    /**
     * Atualiza registro (Produto) no arquivo por meio de id (parâmetro). Retorna true se registro foi atualizado, caso contrário, retorna false.
     * @param p
     * @return boolean - true se registro foi atualizado, caso contrário, retorna false
     * @throws Exception
     */
    public boolean updateElement(Produto p) throws Exception{
        raf.seek(0);
        raf.readInt();
        long pos = 0;

        while(raf.getFilePointer() < raf.length()){
            pos = raf.getFilePointer();
            Produto p2 = readElement();
            if(p2.getId() == p.getId() && p2.getAlive()){
                byte[] bArr = p.toByteArray();
                int len = bArr.length;
                raf.seek(pos+1);
                int len2 = raf.readInt()+5;
                if(len <= len2){
                    raf.seek(pos);
                    raf.write(bArr);
                    raf.seek(pos+1);
                    raf.writeInt(len2-5);
                }else{
                    raf.seek(pos);
                    raf.writeByte((byte)'*');
                    raf.seek(raf.length());
                    raf.write(bArr);
                }

                return true;
            }
        }

        return false;
    }

    public Produto[] conditionalSearch(Predicate<Produto> condition, int max) throws Exception{
        raf.seek(0);
        raf.readInt();
        Produto[] res = new Produto[max];
        int count = 0;
        while(raf.getFilePointer() < raf.length() && count < max){
            Produto p = readElement();
            if(condition.test(p)){
                res[count] = p;
                count++;
            }
        }

        return res;
    }

    /**
     * Reposiciona ponteiro RAF para início do arquivo (pula 4 bytes e parte para o primeiro registro).
     * @throws Exception
     */
    public void resetPosition() throws Exception{
        raf.seek(0);
        raf.readInt();
    }

    /**
     * Lê próximo registro (Produto) do arquivo. Se não houver mais registros, retorna null.
     * @param n
     * @return
     */
    public Produto[] readNext(int n){
        Produto[] res = new Produto[n];
        for(int i=0;i<n;i++){
            try{
                res[i] = readElement();
            }catch(Exception e){
                res[i] = null;
            }
        }

        return res;
    }

    /**
     * Retorna true se houver mais registros (Produtos) no arquivo, caso contrário, retorna false.
     * @return boolean - true se houver mais registros, caso contrário, retorna false
     * @throws Exception
     */
    public boolean hasNext() throws Exception{
        return raf.getFilePointer() < raf.length();
    }

    /**
     * Retorna quantidade de registros (Produtos) no arquivo com base na função hasNext().
     * @return int - quantidade de registros
     * @throws Exception
     */
    public int getProductAmount() throws Exception{
        int amount = 0;
        resetPosition();
        while(hasNext()){
            readElement();
            amount++;
        }

        return amount;
    }

    public long getFilePointerPosition() throws Exception{
        return raf.getFilePointer();
    }

    public HashExtensivel createHashExtensivel(int pInicial, int bucketSize) throws Exception{
        HashExtensivel he = new HashExtensivel("hash.dat", "indice.dat", pInicial, bucketSize);
        raf.seek(4);
        while(raf.getFilePointer() < raf.length()){
            long pos = raf.getFilePointer();
            Produto p = readElement();
            he.inserir(p.getId(), pos);
        }

        return he;
    }

    public Produto findProdutoUsingHash(HashExtensivel he, int id) throws Exception{
        long pos = he.pesquisar(id);
        if(pos == -1){
            return null;
        }

        raf.seek(pos);
        return readElement();
    }
}
