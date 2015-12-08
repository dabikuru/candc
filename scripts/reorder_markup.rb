def replace_f(markedup, new_markedup)

  h = {}

  markedup.each_line do |line|
    case line
      when /^  \d \S+$/
        # puts "LINE #{line}"
        h = {}
        i = 0

        line.each_char.with_index do |c, index|
          if c =~ /\d/
            break if c == '0'
            if i >= 1
              h[c] = i
              line[index] = "#{i}"
            end
            i += 1
          end
        end
      else
        line.each_char.with_index do |c, index|
          if c =~ /\d/ and not h[c].nil?
            line[index] = "#{h[c]}"
          end
        end
    end
    new_markedup.write line
  end
end

if ARGV.length < 2
  $stderr.puts 'Too few arguments'
  exit
end

fin = open(ARGV[0], 'r')
fout = open(ARGV[1], 'w')
replace_f(fin, fout)