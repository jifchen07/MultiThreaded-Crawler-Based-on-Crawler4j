import csv
from collections import defaultdict
from http.client import responses

fetch_file = '../fetch_nytimes.csv';
all_url_file = '../urls_nytimes.csv';
visit_file = '../visit_nytimes.csv';

report_file = 'CrawlReport_nytimes.txt';

'''
calculate fetch statistics
'''
n_fetch_success, n_fetch_fail = 0, 0
code_counts = defaultdict(int)

with open(fetch_file, 'r', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader)
    for row in reader:
        if len(row) != 2:
            continue
        status_code = row[1]
        code_counts[status_code] += 1
        if status_code.startswith('2'):
            n_fetch_success += 1
        else:
            n_fetch_fail += 1

'''
calculate all processes urls statistics
'''
seen = set()
n_total = 0
n_within, n_outside = 0, 0
with open(all_url_file, 'r', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader)
    for row in reader:
        if len(row) != 2:
            continue
        n_total += 1
        url, residency_code = row
        if url in seen:
            continue
        seen.add(url)
        if residency_code == 'OK':
            n_within += 1
        else:
            n_outside += 1

'''
calculate visited urls/pages statistics
'''
size_counts = {'< 1KB': 0,
               '1KB ~ <10KB': 0,
               '10KB ~ <100KB': 0,
               '100KB ~ <1MB': 0,
               '>= 1MB': 0}

type_counts = defaultdict(int)
with open(visit_file, 'r', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader)
    for row in reader:
        if len(row) != 4:
            continue
        url, size, outlinks, type = row
        size = int(size)
        if size < 1:
            size_counts['< 1KB'] += 1
        elif size < 10:
            size_counts['1KB ~ <10KB'] += 1
        elif size < 100:
            size_counts['10KB ~ <100KB'] += 1
        elif size < 1024:
            size_counts['100KB ~ <1MB'] += 1
        else:
            size_counts['>= 1MB'] += 1
        type_counts[type] += 1

'''
write to report file
'''
with open(report_file, 'w') as fw:
    header = 'Fetch Statistics:'
    fw.write('\n' + header + '\n' + '=' * len(header) + '\n' +
             f'# fetches attempted: {n_fetch_success + n_fetch_fail}\n'
             f'# fetches succeeded: {n_fetch_success}\n'
             f'# fetches failed or aborted: {n_fetch_fail}\n')

    header = 'Outgoing URLs:'
    fw.write('\n' + header + '\n' + '=' * len(header) + '\n' +
             f'Total URLs extracted: {n_total}\n'
             f'# unique URLs extracted: {n_within + n_outside}\n'
             f'# unique URLs within News Site: {n_within}\n'
             f'# unique URLs outside News Site: {n_outside}\n')

    header = 'Status Codes:'
    fw.write('\n' + header + '\n' + '=' * len(header) + '\n')
    fw.writelines(f'{code} {responses[int(code)]}: {code_counts[code]}\n' for code in sorted(code_counts))

    header = 'File Sizes:'
    fw.write('\n' + header + '\n' + '=' * len(header) + '\n')
    fw.writelines(f'{sz}: {size_counts[sz]}\n'
                  for sz in ['< 1KB', '1KB ~ <10KB', '10KB ~ <100KB', '100KB ~ <1MB', '>= 1MB'])

    header = 'Content Types:'
    fw.write('\n' + header + '\n' + '=' * len(header) + '\n')
    fw.writelines(f'{t}: {c}\n' for t, c in sorted(type_counts.items(), key=lambda item: item[1], reverse=True))
