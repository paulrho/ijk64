#!/usr/bin/perl
#------------------------------------------------------------------------------
# Script to adjust the last modified time of checked-out files of a git clone
# to match the last commit date on the file.
#
# Expected to be run from the top-level of a git repo being adjusted.
# Also expect that the appropriate branch is already checked-out.
#
# Original code taken from https://stackoverflow.com/questions/2179722/checking-out-old-file-with-original-create-modified-timestamps
#------------------------------------------------------------------------------
 
use strict;
 
my %attributions;
my $remaining = 0;
 
open IN, "git ls-tree -r --full-name HEAD |" or die;
while (<IN>) {
    if (/^\S+\s+blob\s+\S+\s+(.+)$/) {
        $attributions{$1} = -1;
    }
}
close IN;
 
$remaining = (keys %attributions) + 1;
print "Number of files: $remaining\n";
 
my $date;
#open IN, "git log -r --raw --date=unix --pretty=format:%h~%cd~ |" or die;
open IN, "git log -r --raw --date=raw --pretty=format:%h~%cd~ |" or die;
while (<IN>) {
    if (/^[^:~]+~([^~]+)~$/) {
        $date = $1;
    } elsif (/^:\S+\s+\S+\s+\S+\s+\S+\s+\S\s+(.*)$/) {
        if ($attributions{$1} == -1) {
            $attributions{$1} = "$date";
            $remaining--;
 
            print "would set utime ".$date." ".$date." ".$1."\n";
            utime $date, $date, $1;
            if ($remaining % 1000 == 0) {
                print "$remaining\n";
            }
            if ($remaining <= 0) {
                exit;
            }
        }
    }
}
 
close IN;
