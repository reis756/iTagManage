package com.reisdeveloper.itagmanage

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class DeviceLog : AppCompatActivity() {

    private var dateFormat = SimpleDateFormat("hh:mm:ss", Locale.US)
    private val characteristicUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private var bleDevice: BleDevice? = null
    private val bluetoothGattService: BluetoothGattService? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val charaProp = 0
    private var macAddress : String? = null

    companion object {
        private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

        fun newInstance(context: Context, macAddress: String): Intent =
            Intent(context, DeviceLog::class.java).apply { putExtra(EXTRA_MAC_ADDRESS, macAddress) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        connect.setOnClickListener { onConnectClick() }
        write.setOnClickListener { onWriteClick() }
        //notify.setOnClickListener { onNotifyClick() }
        indicate.setOnClickListener { onIndicateClick() }

        macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        title = getString(R.string.mac_address, macAddress)

    }

    private fun onConnectClick() {
        BleManager.getInstance().connect(macAddress, object : BleGattCallback() {
            override fun onStartConnect() {
                printLog("Start connection")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                printLog("Error on connection: ${exception.description}")
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                this@DeviceLog.bleDevice = bleDevice
                bluetoothGatt = gatt
                printLog("Connected")
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                printLog("Disconected")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().clearCharacterCallback(bleDevice)
        //DeviceLog.getInstance().deleteObserver(this)
    }

    private fun onNotifyClick() {
        BleManager.getInstance().notify(
            bleDevice,
            "0000ffe0-0000-1000-8000-00805f9b34fbb",
            "0000ffe1-0000-1000-8000-00805f9b34fb",
            //characteristic?.service?.uuid.toString(),
            //characteristic?.uuid.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    printLog("notify success")
                }

                override fun onNotifyFailure(exception: BleException) {
                    printLog(exception.toString())
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    printLog(HexUtil.formatHexString(characteristic?.value, true))

                }
            })
    }

    private fun onWriteClick() {
        BleManager.getInstance().write(
            bleDevice,
            "00001802-0000-1000-8000-00805f9b34fb",
            "00002a06-0000-1000-8000-00805f9b34fb",
            //characteristic?.service?.uuid.toString(),
            //characteristic?.uuid.toString(),
            HexUtil.hexStringToBytes("hex"),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int,total: Int,justWrite: ByteArray) {
                    printLog("Send Alert!")
                }

                override fun onWriteFailure(exception: BleException) {
                    printLog(exception.toString())
                }
            })
    }

    private fun onIndicateClick() {
        BleManager.getInstance().indicate(
            bleDevice,
            "0000ffe0-0000-1000-8000-00805f9b34fb",
            "0000ffe1-0000-1000-8000-00805f9b34fb",
            //characteristic!!.service.uuid.toString(),
            //characteristic!!.uuid.toString(),
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    printLog("Indicate Alert!")
                }

                override fun onIndicateFailure(exception: BleException) {
                    printLog(exception.toString())
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    printLog("Indicate Alert!")
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun printLog(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val date = Date()
            log.post {
                log.text = "${dateFormat.format(date)} - $message\n--------\n${log.text}"
            }
        }
    }
}