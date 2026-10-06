
# Requests 2.32 supports operation without a character-detection library,
# falling back to UTF-8 for responses without a detected encoding.
PACKAGECONFIG:remove:pn-python3-requests = "chardet"
PACKAGECONFIG:append:pn-python3-requests = " charset-normalizer"
