package Helpers;

public class Animation {
    private String lastLine = "";
    private boolean stop = false;

    public void print(String line) {
        //clear the last line if longer
        if (lastLine.length() > line.length()) {
            String temp = "";
            for (int i = 0; i < lastLine.length(); i++) {
                temp += " ";
            }
            if (temp.length() > 1)
                System.out.print("\r" + temp);
        }
        System.out.print(line);
        lastLine = line;
    }

    private byte anim;

    public void animate(String line) {
        switch (anim) {
            case 1:
                print("[ \\ ] ");
                break;
            case 2:
                print("[ | ] ");
                break;
            case 3:
                print("[ / ] " );
                break;
            default:
                anim = 0;
                print("[ - ] ");
        }
        anim++;
    }

    public void startLoading(){
        for (int i = 0; i < 2000000000; i++) {
            animate(i + "");
            if(stop){
                break;
            }
            //simulate a piece of task
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLoading(){
        stop = true;
    }
}