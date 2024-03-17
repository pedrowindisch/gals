import os

working_dir = os.path.join(os.getcwd(), 'src')
java_files = [
    os.path.join(dp, f) 
    for dp, dn, filenames 
    in os.walk(working_dir) 
    for f in filenames 
    if f.endswith('.java')
]

for java_file in java_files:
    with open(java_file, 'r', encoding='windows-1252') as f:
        content = f.read()

    with open(java_file, 'w', encoding='utf-8') as f:
        f.write(content)