package personal.cx.point.thread

class SearchThread () : Thread() {

    private var if_stop : Boolean = false

    //线程
    override fun run() {
        while(true){
            if(if_stop == false){

                Thread.sleep(1000)
            }

        }
    }

    //关闭线程
    fun close(){
        this.if_stop = true
    }
}
