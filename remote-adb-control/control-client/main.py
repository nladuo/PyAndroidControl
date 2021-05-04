from remote_adb_api import *


devices = get_devices()
print(devices)


if len(devices) > 0:
    first_device = devices[0]
    get_screenshot(first_device)

    send_commands(first_device, [
        'input text "111"',
        'input text "222"',
        'input text "333"',
    ])
