This project trains on the following version of the openwebtext dataset:

https://huggingface.co/datasets/Skylion007/openwebtext

The easiest way to download it is through the python huggingface cli:

`pip install -U huggingface_hub[cli]`

`hf download Skylion007/openwebtext --repo-type dataset --local-dir ./data/openwebtext-full`

To run on GPU (Windows 11):
- Install the Visual Studio C++ Build Tools
- Install the latest CUDA Toolkit