# Bluebird RFID Integrated Android SDK — Developer Documentation (LIB 5.80.27.24)

Consolidated from javadoc (co.kr.bluebird.sled) and PDFs:
- Bluebird_RFID_INTEGRATED_Android_SDK_20250908_LIB_5.80.27.24.pdf
- Bluebird_RFID_INTEGRATED_Android_SDK_ReleaseNotes_20250908_LIB_5.80.27.24.pdf

This guide covers setup, connection flows, core APIs, events, updates, and common pitfalls for Serial (`Reader`) and Bluetooth (`BTReader`) SLEDs.

## Scope & Versions
- Library: 5.80.27.24 (Rev 1.4.6, 2025‑09‑08)
- Main package: `co.kr.bluebird.sled`
- Serial: `Reader` (+ `ISerialManager`)
- Bluetooth: `BTReader` (+ `IBluetoothManager`)
- RFID interfaces: `IRfidInventory`, `IRfidAccess`, `IRfidConfig`
- SLED interfaces: `ISledConfig`, `ISledCommunicationManager`
- Barcode: `IBarcodeController`, `ISledBarcodeController`
- Constants & messages: `SDConsts` nested classes
- EPC tools: `co.kr.bluebird.sled.epc_convert` (`EPCConvert`, `EPCData`)

## Install & Setup
- Add SDK JAR/AAR to app module.
- Permissions (minimum):
  - Bluetooth flows: `android.Manifest.permission.BLUETOOTH` (add modern BT permissions per targetSdk when needed).
  - Updates (files): `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (prefer Uri overloads on new Android).
- SLED attach/detach broadcast (Bluebird devices):
  - Actions: `kr.co.bluebird.android.sled.action.SLED_ATTACHED` / `SLED_DETACHED`.
  - Register a `BroadcastReceiver` in manifest or code.

## Connect & Disconnect Flows
- Serial SLED (`Reader`):
  - App active: `SD_Open(clientId)` → `SD_Wakeup()` (wait `SLED_WAKEUP`) → `SD_Connect()`.
  - App inactive: `SD_Disconnect()` → `SD_Close()`.
  - `SD_Wakeup()` ≈800ms; some models support `SD_WakeupFaster()`.
- Bluetooth SLED (`BTReader`):
  - Ensure BT enabled (`BT_Enable()` on Bluebird A13+ or via system).
  - Connect: `BT_Connect(address)` or `BT_Connect(address, deviceType)` (faster; see `SDConsts.BTDeviceType`).
  - Disconnect: `BT_Disconnect()`.

## Event Model (Handler)
- Channels (`SDConsts.Msg`):
  - `RFMsg` — inventory/access/firmware events (see `SDConsts.RFCmdMsg`).
  - `SDMsg` — trigger/wakeup/SLED FW events (see `SDConsts.SDCmdMsg`).
  - `BCMsg` — barcode events.
- Typical handling: switch on `msg.what` then `msg.arg1` against `RFCmdMsg`/`SDCmdMsg`.

## RFID Inventory (IRfidInventory)
- `RF_PerformInventory(turbo, enableSelection, ignorePC)` — core inventory.
- Variant: `RF_PerformInventory(..., isEPCDecoder)` — include EPC decoded data.
- Locating: `RF_PerformInventoryWithLocating(...)` or `RF_PerformInventoryForLocating(epc)`.
- Phase/Frequency: `RF_PerformInventoryWithPhaseFreq(...)`.
- RSSI‑limit: `RF_PerformInventoryWithRssiLimitation(..., rssiLimitation)`.
- Stop: `RF_StopInventory()`.
- Encoding inventory (1.4.4+): `RF_SetEncodeInformation(...)`, `RF_PerformInventoryEncoding(...)`, `RF_StopInventoryEncoding()`.

## RFID Access (IRfidAccess)
- `RF_READ(RFMemType, start, length, accessPwd, enableSelection)`
- `RF_WRITE(RFMemType, start, data, accessPwd, enableSelection)`
- Block ops: `RF_BlockWrite`, `RF_BlockErase`, `RF_BlockPermalock`.
- Bulk: `RF_BulkWrite(...)`.
- Passwords/ID: `RF_WriteAccessPassword`, `RF_WriteKillPassword`, `RF_WriteTagID`.
- Lock/Kill: `RF_LOCK(...)`, `RF_KILL(...)`.

## RFID Configuration (IRfidConfig)
- Timeouts/duty/dwell: `RF_SetAccessTimeout`, `RF_SetINVTimeout`, `RF_SetDutyCycle`, `RF_SetDwelltime`.
- Power/mode: `RF_SetRadioPowerState(5..30)`, `RF_SetRFMode(0..3)` (Mode 4 on E710 + specific FW).
- Singulation: `RF_Get/SetSingulationControl(...)` (+ min/max).
- Session/Toggle: `RF_SetSession(...)`, `RF_SetToggle(0/1)`.
- Protocol (1.4.3+): `RF_Set/GetRFIDProtocolType()` (Gen2/Gen2X).
- DYN: `RF_SetDYNRFMode(0/1)`, `RF_SetDYNStartQ`, `RF_SetDYNModeSquence`, `RF_SetDYNModeMinMaxMode`.
- DYN update: `RF_UpdateDYNProfile(String|none)` (~90s; callbacks `UPDATE_RF_DYN_MAC_*`).
- RSSI tracking & channels: `RF_Get/SetRssiTrackingState`, `RF_GetEnableChannels`, `RF_GetDefaultChannels`.
- LBT (RFR901): `RF_Get/SetLBTValue`.
- Region: `RF_Get/SetRegion`, `RF_GetAvailableRegionAtThisDevice()`, `RF_checkRegionISO(iso)`.

## SLED Configuration & Updates
- Info: `SD_GetVersion`, `SD_GetBootLoaderVersion`, `SD_GetModel`, `SD_GetType`, `SD_GetSerialNumber`, `SD_GetHostSerialNumber`.
- Battery: `SD_GetBatteryStatus` + Smart Battery getters (capacity, cycle, health, level, lifetime, voltage, present, temp, status).
- Trigger/Indicators: `SD_SetTriggerMode`, `SD_Set/GetTriggerKeyEnable`, `SD_SetBuzzerEnable/Level`, `SD_GetBuzzerState/Level`, `SD_SetLEDEnable`, `SD_GetLEDEnableState`, `SD_SetTagBuzzerEnable/Sound`, `SD_GetTagBuzzerState`, `SD_Set/GetAutoSleepTimeout`.
- SLED FW: `SD_UpdateSLEDFirmware(String|Uri)`, smart/combined variants; callbacks `UPDATE_SD_FW_*`.
- Open/Close: `SD_Open(String clientId)`, `SD_Close()`.
- Serial region info: `SD_SetRegionInfo(path)`, `SD_SetRegionInfoVersion(int)`, `SD_GetRegionInfoVersion()`.

## Bluetooth (IBluetoothManager)
- `BT_Enable`, `BT_Disable`, `BT_IsEnabled`, `BT_GetConnectState`.
- `BT_StartScan`, `BT_StopScan`, `BT_GetPairedDevices`.
- `BT_Connect(address)` or `BT_Connect(address, deviceType)`, `BT_Disconnect`.
- Connected info: `BT_GetConnectedDeviceName`, `BT_GetConnectedDeviceAddr`.
- Many APIs return `BLUETOOTH_NOT_ENABLED = -15` when BT disabled.

## Barcode
- Host‑device barcode (BC_): trigger/pause/resume/state; key/trigger modes; multi‑scan; device capability `BC_GetSupportedDevicesInfo` (1.4.6).
- SLED barcode (SB_): enable scanner/aim/illumination; presets/params; `SB_StartScan`; capability `SB_GetSupportedDevicesInfo` (1.4.6).

## EPC Utilities
- `EPCConvert.getInstance().decode(String)` → `EPCData.getDecodeData()`.

## Return Codes & Common Errors
- Success: 0. Errors: negative (see `SDConsts.*` families: `RFResult`, `SDResult`, `RFAccessTimeout`, etc.).
- `OTHER_CMD_RUNNING_ERROR = -4`: wait for current op to finish.
- `SD_NOT_CONNECTED = -5`: connect first (`SD_Connect` or `BT_Connect`).
- `MODE_ERROR = -6`: operation not valid in current mode.
- Low battery limits (RFID ~7%, barcode ~4%).
- Bluetooth disabled: `-15`.

## Minimal Examples
### Serial Reader
```java
Reader r = new Reader(context, handler);
if (!r.SD_Open("clientId")) return;
r.SD_Wakeup(); // wait SLED_WAKEUP in handler, then:
r.SD_Connect();
r.RF_SetRegion(SDConsts.RFRegion.FCC);
r.RF_SetRadioPowerState(30);
r.RF_PerformInventory(true, false, true);
r.RF_StopInventory();
r.SD_Disconnect();
r.SD_Close();
```

### Bluetooth Reader
```java
BTReader br = new BTReader(context, handler);
br.BT_Enable();
br.BT_Connect("AA:BB:CC:DD:EE:FF");
br.RF_PerformInventory(false, false, true);
br.RF_StopInventory();
br.BT_Disconnect();
```

## Release Notes (highlights)
- 1.4.6: `SB_GetSupportedDevicesInfo`, `BC_GetSupportedDevicesInfo`.
- 1.4.4: encoding inventory APIs; E710 unknown code (‑48).
- 1.4.3: protocol enable/disable; carrier wave start/stop.
- 1.3.0: A13+ allow `BT_Enable/Disable`; RSSI limit range to ‑100.
- 1.2.0: `RF_BulkWrite`, `RF_WriteSwitchMode`; region/channel/power fixes; smarter handler messages.

## Troubleshooting & Tips
- Query and set region on startup: `RF_GetAvailableRegionAtThisDevice()` → `RF_SetRegion(...)`.
- Serialize RF commands; stop inventory before access ops.
- Prefer Uri overloads for FW/DYN updates (~90s, progress via callbacks).
- Turbo (duty=0) is heavy; tune duty/dwell/power for thermals/battery.
- Handle triggers (`TRIGGER_PRESSED/RELEASED`) if using hardware trigger modes.

