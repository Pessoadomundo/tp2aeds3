package arvore;

import java.io.RandomAccessFile;
import java.util.Random;
import java.util.RandomAccess;

public class Pagina {
    boolean isFolha;
    int pos;
    int left;
    int right;
    short qtdElementos;
    int[] filhos;
    int[] ids;
    long[] posicoes;

    public Pagina(int ordem) {
        isFolha = true;
        pos = -1;
        left = -1;
        right = -1;
        qtdElementos = 0;
        ids = new int[ordem - 1];
        posicoes = new long[ordem - 1];
        filhos = new int[ordem];
        for(int i = 0; i < ordem; i++) {
            filhos[i] = -1;
        }
        for(int i = 0; i < ordem - 1; i++) {
            ids[i] = -1;
            posicoes[i] = -1;
        }
    }

    public Pagina(RandomAccessFile raf, int ordem) throws Exception{
        this.filhos = new int[ordem];
        this.ids = new int[ordem - 1];
        this.posicoes = new long[ordem - 1];
        
        this.pos = (int) raf.getFilePointer();    
        this.isFolha = raf.readByte() == (byte)'#';
        this.qtdElementos = raf.readShort();
        
        if(!this.isFolha) {
            this.left = raf.readInt();
            this.right = raf.readInt();
            for(int i = 0; i < ordem-1; i++) {
                this.filhos[i] = raf.readInt();
                this.ids[i] = raf.readInt();
            }
            this.filhos[ordem-1] = raf.readInt();
        }else{
            for(int i = 0; i < ordem-1; i++) {
                this.ids[i] = raf.readInt();
                this.posicoes[i] = raf.readLong();
            }
        }
        
    }

    public void salvar(RandomAccessFile raf, int ordem) throws Exception {
        raf.seek(this.pos);
        raf.writeByte(this.isFolha ? (byte)'#' : (byte)' ');
        raf.writeShort(this.qtdElementos);
        
        if(!this.isFolha) {
            raf.writeInt(this.left);
            raf.writeInt(this.right);
            for(int i = 0; i < ordem-1; i++) {
                raf.writeInt(this.filhos[i]);
                raf.writeInt(this.ids[i]);
            }
            raf.writeInt(this.filhos[ordem-1]);
        }else{
            for(int i = 0; i < ordem-1; i++) {
                raf.writeInt(this.ids[i]);
                raf.writeLong(this.posicoes[i]);
            }
        }
    }

    public Pagina inserir(int id, long pos, RandomAccessFile raf, int ordem) throws Exception{
        if(this.isFolha){
            if(this.qtdElementos < ordem - 1){
                int i = 0;
                while(i < this.qtdElementos && this.ids[i] < id){
                    i++;
                }
                for(int j = this.qtdElementos; j > i; j--){
                    this.ids[j] = this.ids[j-1];
                    this.posicoes[j] = this.posicoes[j-1];
                }
                this.ids[i] = id;
                this.posicoes[i] = pos;
                this.qtdElementos++;
                this.salvar(raf, ordem);
                return this;
            }
            Pagina novaPagina = new Pagina(ordem);
            novaPagina.pos = (int)raf.length();
            novaPagina.isFolha = true;
            novaPagina.qtdElementos = (short)(ordem/2);
            for(int i = 0; i < ordem/2; i++){
                novaPagina.ids[i] = this.ids[i + ordem/2];
                novaPagina.posicoes[i] = this.posicoes[i + ordem/2];
            }
            this.qtdElementos = (short)(ordem/2);
            this.salvar(raf, ordem);
            raf.seek(novaPagina.pos);
            novaPagina.salvar(raf, ordem);
            return novaPagina;
        }

        int i = 0;
        while(i < this.qtdElementos && this.ids[i] < id){
            i++;
        }
        raf.seek(this.filhos[i]);
        Pagina pagina = new Pagina(raf, ordem);
        pagina = pagina.inserir(id, pos, raf, ordem);
        if(pagina == null){
            return null;
        }
        if(this.qtdElementos < ordem - 1){
            for(int j = this.qtdElementos; j > i; j--){
                this.ids[j] = this.ids[j-1];
                this.filhos[j+1] = this.filhos[j];
            }
            this.ids[i] = pagina.ids[0];
            this.filhos[i+1] = pagina.pos;
            this.qtdElementos++;
            this.salvar(raf, ordem);
            return this;
        }
        Pagina novaPagina = new Pagina(ordem);
        novaPagina.pos = (int)raf.length();
        novaPagina.isFolha = false;
        novaPagina.qtdElementos = 1;
        novaPagina.ids[0] = pagina.ids[0];
        novaPagina.filhos[0] = this.pos;
        novaPagina.filhos[1] = pagina.pos;
        raf.seek(novaPagina.pos);
        novaPagina.salvar(raf, ordem);
        return novaPagina;
    }

    public String toString(){
        String str = "";
        str += "IsFolha: " + this.isFolha + "\n";
        str += "Pos: " + this.pos + "\n";
        str += "Left: " + this.left + "\n";
        str += "Right: " + this.right + "\n";
        str += "QtdElementos: " + this.qtdElementos + "\n";
        str += "Filhos: ";
        for(int i = 0; i < this.filhos.length; i++){
            str += this.filhos[i] + " ";
        }
        str += "\n";
        str += "Ids: ";
        for(int i = 0; i < this.ids.length; i++){
            str += this.ids[i] + " ";
        }
        str += "\n";
        str += "Posicoes: ";
        for(int i = 0; i < this.posicoes.length; i++){
            str += this.posicoes[i] + " ";
        }
        str += "\n";
        return str;
    }
}
