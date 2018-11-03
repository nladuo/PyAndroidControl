"""
    Write you own logic to control the remote Android Device

    For example:
        input text "xxxx"   # send keyboard event
        input tap 50 250    # click event
"""


def get_commands(token, img_file):
    """ analyze the screenshot and send
        commands back to android device """
    img_file.save("tmp.png")
    print(token)

    # your adb shell commands
    cmds = [
        'input text "111"',
        'input text "222"',
        'input text "333"',
    ]
    return cmds
