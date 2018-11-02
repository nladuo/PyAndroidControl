# PyAndroidControl
Control your Android device via python scripts.

## Architecture
![](architecture.png)

## Usage
### 1. Install the App
Install the [PyAndroidControl App](https://github.com/nladuo/PyAndroidControl/releases), and make sure your device has been **"root"**.

### 2. Write your Logics
Change the [simple-control-server/logics.py](simple-control-server/logics.py) to what ever you want.
```
def get_commands(token, img_file):
    """  """
    img_file.save("tmp.png")
    print(token)

    # your adb shell commands
    cmds = [
        'input text "111"',
        'input text "222"',
        'input text "333"',
    ]
    return cmds
```
### 3. Demo
Automatically input text "111", "222", "333".
![](demo.gif)
## License
MIT
