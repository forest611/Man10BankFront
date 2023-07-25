package red.man10.man10bank

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import red.man10.man10bank.api.APIBank
import red.man10.man10bank.util.Utility.msg

//サーバーの接続状況や各機能のon/offを確認するクラス
class Status : CommandExecutor{

    var enableDealBank = false
    var enableATM = false
    var enableCheque = false
    var enableLocalLoan = false
    var enableServerLoan = false

    companion object{
        var status = Status()

        private var timerThread = Thread()

        private fun asyncSendStatus(){
            Man10Bank.async.execute {
                APIBank.setStatus(status)
            }
        }

        private fun getStatus(){
            status = APIBank.getStatus()
        }

        fun startStatusTimer(){
            timerThread = Thread{
                Bukkit.getLogger().info("ステータスチェク処理を走らせます")
                try {
                    getStatus()
                    Thread.sleep(1000L * Config.statusCheckSeconds)
                }catch (e:InterruptedException){
                    Bukkit.getLogger().info("ステータスチェック処理を終了")
                    return@Thread
                }
            }
            timerThread.start()
        }

        fun stopStatusTimer(){
            timerThread.interrupt()
        }

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (label != "bankstatus")return false
        if (!sender.hasPermission(Permissions.BANK_OP_COMMAND))return false
        if (args.isNullOrEmpty()){
            msg(sender,"現在の稼働状況")
            msg(sender,"Man10BankServer:${Man10Bank.isEnableServer()}")

            msg(sender,"enableDealBank:$enableDealBank")
            msg(sender,"enableATM:$enableATM")
            msg(sender,"enableCheque:$enableCheque")
            msg(sender,"enableServerLoan:$enableServerLoan")
            msg(sender,"enableLocalLoan:$enableLocalLoan")

            return true
        }

        if (args[0] == "set" && args.size == 3){
            try {

                val value = args[2].toBoolean()

                when(args[1]){
                    "all" -> {
                        enableDealBank = value
                        enableATM = value
                        enableCheque = value
                        enableLocalLoan = value
                        enableServerLoan = value
                    }
                    "enableDealBank" -> enableDealBank = value
                    "enableATM" -> enableATM = value
                    "enableCheque" -> enableCheque = value
                    "enableLocalLoan" -> enableLocalLoan = value
                    "enableServerLoan" -> enableServerLoan = value
                    else ->{
                        msg(sender,"無効なステータス")
                    }
                }

                asyncSendStatus()
                msg(sender,"設定完了")
            }catch (e:Exception){
                msg(sender,"引数に問題あり")
            }
        }

        return true
    }

}