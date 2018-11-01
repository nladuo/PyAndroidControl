"""
    Write you own logic to control the remote Android Device

    For example:
        input text "xxxx"   # send keyboard event
        input tap 50 250    # click event
"""


def get_commands(token, img_file):
    """  """
    img_file.save("tmp.png")
    print(token)

    cmds = [
        'input text "111"',
        'input text "222"',
        'input text "333"',
    ]
    return cmds
