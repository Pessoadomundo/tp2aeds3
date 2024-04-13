package arvore;

import java.io.RandomAccessFile;

public class ArvoreB {
    public static final int ORDEM = 8;
    public static final int TAMANHO_PAGINA = 150;
    long raizPos;
    RandomAccessFile raf;

    public ArvoreB(){
        raizPos = -1;
        raf = null;
    }

    public ArvoreB(String arq) throws Exception{
        raf = new RandomAccessFile(arq, "rw");
        raizPos = 8;
        if(raf.length() == 0){
            raf.writeLong(raizPos);
            raf.write(new Pagina().toByteArray());
        }else{
            raizPos = raf.readLong();
        }
    }

    public void start(String arq) throws Exception{
        raf = new RandomAccessFile(arq, "rw");
        raizPos = 8;
        raf.setLength(0);
        raf.seek(0);
        raf.writeLong(raizPos);
        raf.write(new Pagina().toByteArray());
    }

    public byte[] readPagina(long pos) throws Exception{
        byte[] b = new byte[TAMANHO_PAGINA];
        raf.seek(pos);
        raf.read(b);
        return b;
    }

    public void writePagina(long pos, Pagina p) throws Exception{
        raf.seek(pos);
        raf.write(p.toByteArray());
    }

    public void writePaginaEnd(Pagina p) throws Exception{
        raf.seek(raf.length());
        raf.write(p.toByteArray());
    }



    public Dupla inserir(int chave, long posicao, long posPagina) throws Exception{
        Pagina p = Pagina.fromByteArray(readPagina(posPagina));
        if(p.isFolha){
            if(!p.isCheia()){
                //folha que cabe mais elementos
                p.inserirElemento(chave, posicao);
                raf.seek(posPagina);
                raf.write(p.toByteArray());
                return null;
            }
            //folha que nao cabe mais elementos
            Pagina nova = new Pagina();
            Dupla[] duplas = new Dupla[p.nElementos+1];
            for(int i = 0; i < p.nElementos; i++){
                duplas[i] = new Dupla(p.chaves[i], p.posicoes[i]);
            }
            duplas[p.nElementos] = new Dupla(chave, posicao);
            Dupla.sort(duplas);
            for(int i = 0; i < (ORDEM)/2; i++){
                p.chaves[i] = duplas[i].chave;
                p.posicoes[i] = duplas[i].posicao;
            }
            p.nElementos = (ORDEM)/2;
            nova.isFolha = true;
            nova.nElementos = (ORDEM-1)/2;
            for(int i = 0; i < (ORDEM-1)/2; i++){
                nova.chaves[i] = duplas[i+(ORDEM)/2+1].chave;
                nova.posicoes[i] = duplas[i+(ORDEM)/2+1].posicao;
            }
            
            nova.filhos[0] = p.filhos[0];
            for(int i = 0; i < (ORDEM-1)/2; i++){
                nova.filhos[i+1] = 0;
            }
            writePagina(posPagina, p);
            writePaginaEnd(nova);

            return new Dupla(duplas[(ORDEM)/2].chave, duplas[(ORDEM)/2].posicao);
        }

        //nao é folha
        int i = 0;
        while(i < p.nElementos && chave > p.chaves[i]){
            i++;
        }
        Dupla d = inserir(chave, posicao, p.filhos[i]);
        if(d == null){
            return null;
        }

        if(!p.isCheia()){
            //pagina que cabe mais elementos
            int pos = p.inserirElemento(d.chave, d.posicao);
            p.filhos[pos+1] = raf.length() - TAMANHO_PAGINA;
            raf.seek(posPagina);
            raf.write(p.toByteArray());

            return null;
        }

        //pagina que nao cabe mais elementos
        Dupla[] duplas = new Dupla[p.nElementos+1];
        for(int j = 0; j < p.nElementos; j++){
            duplas[j] = new Dupla(p.chaves[j], p.posicoes[j]);
        }
        duplas[p.nElementos] = d;
        Dupla.sort(duplas);
        Pagina nova = new Pagina();
        for(int j = 0; j < (ORDEM)/2; j++){
            p.chaves[j] = duplas[j].chave;
            p.posicoes[j] = duplas[j].posicao;
        }
        p.nElementos = (ORDEM)/2;
        for(int j = 0; j < (ORDEM-1)/2; j++){
            nova.chaves[j] = duplas[j+(ORDEM)/2+1].chave;
            nova.posicoes[j] = duplas[j+(ORDEM)/2+1].posicao;
        }
        nova.nElementos = (ORDEM-1)/2;
        nova.isFolha = false;
        nova.filhos[0] = p.filhos[(ORDEM-1)/2+1];
        for(int j = 0; j < (ORDEM-1)/2; j++){
            nova.filhos[j+1] = raf.length() - TAMANHO_PAGINA;
        }
        writePagina(posPagina, p);
        writePaginaEnd(nova);

        return new Dupla(duplas[(ORDEM)/2].chave, duplas[(ORDEM)/2].posicao);
    }

    public void inserir(int chave, long posicao) throws Exception{
        Dupla d = inserir(chave, posicao, raizPos);
        if(d == null){
            return;
        }

        Pagina nova = new Pagina();
        nova.isFolha = false;
        nova.nElementos = 1;
        nova.chaves[0] = d.chave;
        nova.posicoes[0] = d.posicao;
        nova.filhos[0] = raizPos;
        nova.filhos[1] = raf.length() - TAMANHO_PAGINA;
        writePaginaEnd(nova);
        raizPos = raf.length() - TAMANHO_PAGINA;
        raf.seek(0);
        raf.writeLong(raizPos);
    }

    public void printAll() throws Exception{
        raf.seek(8);
        while(raf.getFilePointer() < raf.length()){
            System.out.println("Pos: " + raf.getFilePointer());
            Pagina p = Pagina.fromByteArray(readPagina(raf.getFilePointer()));
            p.print();
            System.out.println("----");
            System.out.println();
        }
    }

    public boolean buscar(int chave) throws Exception{
        return buscar(chave, raizPos);
    }

    public boolean buscar(int chave, long pos) throws Exception{
        Pagina p = Pagina.fromByteArray(readPagina(pos));
        if(p.isFolha){
            for(int i = 0; i < p.nElementos; i++){
                if(p.chaves[i] == chave){
                    return true;
                }
            }
            return false;
        }

        int i = 0;
        while(i < p.nElementos && chave > p.chaves[i]){
            i++;
        }
        if(p.chaves[i] == chave){
            return true;
        }
        return buscar(chave, p.filhos[i]);
    }

    public static void main(String[] args) {
        ArvoreB arvore = new ArvoreB();
        try{
            arvore.start("arvore.dat");
            for(int i = 1; i <= 43; i++){
                arvore.inserir(i, i*100);
            }
            arvore.printAll();

            for(int i = 1; i <= 43; i++){
                System.out.println(i+": "+arvore.buscar(i));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}